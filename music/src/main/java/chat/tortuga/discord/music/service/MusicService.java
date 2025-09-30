package chat.tortuga.discord.music.service;

import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.config.Music;
import chat.tortuga.discord.music.exception.SameVoiceChannelRequiredException;
import chat.tortuga.discord.music.exception.VoiceChannelRequiredException;
import chat.tortuga.discord.music.service.listener.VoiceConnectionListener;
import chat.tortuga.discord.music.service.playlist.GuildPlayer;
import chat.tortuga.discord.music.service.playlist.message.PlayerMessage;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MusicService {

    private static final String CACHE_MANAGERS = "managers";

    @CacheName(CACHE_MANAGERS)
    Cache cache;

    private final Music music;
    private AudioPlayerManager player;

    @PostConstruct
    void init() {
        player = new DefaultAudioPlayerManager();
        if (music.sources().soundcloud()) player.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        if (music.sources().bandcamp())   player.registerSourceManager(new BandcampAudioSourceManager(true));
        if (music.sources().nicoaudio())  player.registerSourceManager(new NicoAudioSourceManager());
        if (music.sources().vimeo())      player.registerSourceManager(new VimeoAudioSourceManager());
        if (music.sources().yandex())     player.registerSourceManager(new YandexMusicAudioSourceManager());
        if (music.sources().beampro())    player.registerSourceManager(new BeamAudioSourceManager());
        if (music.sources().getyarn())    player.registerSourceManager(new GetyarnAudioSourceManager());
        if (music.sources().twitch())     player.registerSourceManager(new TwitchStreamAudioSourceManager());
        if (music.sources().http())       player.registerSourceManager(new HttpAudioSourceManager());
        if (music.sources().local())      player.registerSourceManager(new LocalAudioSourceManager());

        // TODO add local source downloading
        // TODO add local source querying

        if (music.sources().youtube()) {
            final Music.Youtube.Oauth oauth = music.youtube().oauth();
            final Optional<Music.Youtube.Cipher> cipher = music.youtube().cipher();
            YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(music.youtube().clients().instances());

            if (oauth.enabled()) yt.useOauth2(oauth.token().orElse(null), oauth.token().isPresent());
            cipher.ifPresent(c -> yt.setRemoteCipherManagerUrlPass(c.url(), c.password().orElse(null)));

            player.registerSourceManager(yt);
        }
    }

    @Scheduled(every = "${music.disconnect-on-idle.check-every:off}")
    void disconnectOnIdle() {
        getGuildPlayers().stream()
                .filter(p -> p.getLastEndOfPlaylist() != null)
                .filter(p -> Instant.now().plus(music.disconnectOnIdle().idleFor()).isAfter(p.getLastEndOfPlaylist()))
                .forEach(p -> disconnectFromVoiceChannel(p.getGuild()));
    }

    // region player commands
    public void handleUserQuery(Guild guild, Long channelId, Member member, String query) throws BotException {
        if (guild.getAudioManager().isConnected()) {
            validateSameVoiceChannel(member);
        }
        else {
            connectToVoiceChannel(retrieveMemberVoiceChannel(member));
        }
        GuildPlayer manager = getGuildPlayer(guild, channelId);
        player.loadItemOrdered(manager, query, new TrackLoadResultHandler(manager, member, music, query.contains("&start_radio=")));
    }

    public void handleStop(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild)
                .ifPresent(p -> {
                    p.stop();
                    destroyGuildPlayer(p);
                });
    }

    public void handleSkip(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild).ifPresent(GuildPlayer::nextTrack);
    }

    public void handlePrevious(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild).ifPresent(GuildPlayer::previousTrack);
    }

    public void handlePause(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild).ifPresent(GuildPlayer::togglePause);
    }

    public void handleLooping(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild).ifPresent(GuildPlayer::toggleLooping);
    }

    public void handleLoopingPlaylist(Guild guild, Member member) throws BotException {
        validateSameVoiceChannel(member);
        getGuildPlayerIfExists(guild).ifPresent(GuildPlayer::toggleLoopingPlaylist);
    }

    public void handleDisconnect(Guild guild) {
        getGuildPlayerIfExists(guild)
                .ifPresent(p -> {
                    p.stop();
                    destroyGuildPlayer(p);
                });
    }

    // endregion
    // region state
    public Button getButtonWithCurrentState(Guild guild, String buttonId) {
        final Optional<GuildPlayer> optional = getGuildPlayerIfExists(guild);
        if (optional.isEmpty() || optional.map(GuildPlayer::getPlayerMessageId).isEmpty()) return null;

        final GuildPlayer manager = optional.get();
        final boolean paused = manager.getPlayer().isPaused();
        final boolean looping = manager.isLooping();
        final boolean loopingPlaylist = manager.isLoopingPlaylist();

        return PlayerMessage.getButton(buttonId, paused, looping, loopingPlaylist);
    }

    public boolean isMessageManaged(Guild guild, Long messageId) {
        return getGuildPlayerIfExists(guild).map(p -> p.isMessageManaged(messageId)).orElse(false);
    }
    // endregion
    // region guild players
    protected List<GuildPlayer> getGuildPlayers() {
        final CaffeineCache caffeine = cache.as(CaffeineCache.class);
        return caffeine.keySet()
                .stream()
                .map(caffeine::getIfPresent)
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error("Error retrieving guild players", e);
                    }
                    return null;
                })
                .filter(GuildPlayer.class::isInstance)
                .map(o -> (GuildPlayer) o)
                .toList();
    }

    protected Optional<GuildPlayer> getGuildPlayerIfExists(Guild guild) {
        return Optional.ofNullable(cache.as(CaffeineCache.class).getIfPresent(guild.getIdLong()))
                .map(future -> {
                    try {
                        return (GuildPlayer) future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error("[{}] Error retrieving guild player", guild.getName(), e);
                    }
                    return null;
                });
    }

    protected GuildPlayer getGuildPlayer(final Guild guild, Long channelId) {
        return cache.get(guild.getIdLong(), i -> new GuildPlayer(guild, player, channelId)).await().atMost(Duration.ofSeconds(1));
    }

    protected void destroyGuildPlayer(GuildPlayer guildPlayer) {
        disconnectFromVoiceChannel(guildPlayer.getGuild());
        cache.invalidate(guildPlayer.getGuild().getIdLong()).await().atMost(Duration.ofSeconds(3));
    }
    // endregion
    // region voice channel
    public void disconnectFromVoiceChannel(Guild guild) {
        final AudioManager audioManager = guild.getAudioManager();
        if (!audioManager.isConnected()) return;
        log.info("[{}] Disconnecting from {}", guild.getName(), Optional.ofNullable(audioManager.getConnectedChannel())
                .map(Channel::getName)
                .orElse("voice channel"));
        audioManager.closeAudioConnection();
    }

    protected void connectToVoiceChannel(VoiceChannel channel) {
        final AudioManager audioManager = channel.getGuild().getAudioManager();
        if (audioManager.isConnected()) return;
        audioManager.openAudioConnection(channel);
        audioManager.setConnectionListener(new VoiceConnectionListener(this, channel.getGuild()));
        log.info("[{}] Connected to {}", channel.getGuild().getName(), channel.getName());
    }

    private VoiceChannel retrieveMemberVoiceChannel(Member member) throws VoiceChannelRequiredException {
        return Optional.ofNullable(member.getVoiceState())
                .map(GuildVoiceState::getChannel)
                .map(AudioChannelUnion::asVoiceChannel)
                .orElseThrow(VoiceChannelRequiredException::new);
    }

    private void validateSameVoiceChannel(Member member) throws SameVoiceChannelRequiredException, VoiceChannelRequiredException {
        final AudioManager manager = member.getGuild().getAudioManager();
        final long channelId = retrieveMemberVoiceChannel(member).getIdLong();

        Optional.ofNullable(manager.getConnectedChannel())
                .map(AudioChannelUnion::asVoiceChannel)
                .map(VoiceChannel::getIdLong)
                .filter(bot -> bot.equals(channelId))
                .orElseThrow(SameVoiceChannelRequiredException::new);
    }
    // endregion
}
