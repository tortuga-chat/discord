package chat.tortuga.discord.service.music;

import chat.tortuga.discord.config.ConfigLoader;
import chat.tortuga.discord.config.Music;
import chat.tortuga.discord.exception.BotException;
import chat.tortuga.discord.exception.VoiceChannelRequiredException;
import chat.tortuga.discord.service.music.handler.DefaultAudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MusicService {

    private static final String YOUTUBE_QUERY = "ytsearch: ";
    private static final AudioPlayerManager PLAYER;
    private static final Map<Long, GuildMusicManager> MANAGERS = new ConcurrentHashMap<>();

    private static final Music CONFIG = new ConfigLoader().load(Music.FILE, Music.class);
    private static final Music.Youtube.Oauth OAUTH = CONFIG.getYoutube().getOauth();

    static {
        PLAYER = new DefaultAudioPlayerManager();
        var ytSource = new dev.lavalink.youtube.YoutubeAudioSourceManager(
//                new Tv(), new TvHtml5Embedded(),
                new TvHtml5EmbeddedWithThumbnail(),
                new MusicWithThumbnail(), new WebWithThumbnail(), new MWebWithThumbnail(), new WebEmbeddedWithThumbnail()
        );
        if (OAUTH.isEnabled()) {
            ytSource.useOauth2(OAUTH.getToken(), !OAUTH.isInit());
        }
        PLAYER.registerSourceManager(ytSource);
        //noinspection deprecation - we're actually excluding the deprecated class here
        AudioSourceManagers.registerRemoteSources(PLAYER, YoutubeAudioSourceManager.class, TwitchStreamAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(PLAYER);
    }

    public static void enqueue(Member member, String query) throws BotException {
        final Guild guild = member.getGuild();
        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        connectToVoiceChannel(retrieveMemberVoiceChannel(member));
        try {
            if (!member.getGuild().getAudioManager().isConnected())
                Thread.sleep(500);
            new URI(query);
        } catch (URISyntaxException e) {
            query = YOUTUBE_QUERY.concat(query);
        } catch (InterruptedException e) {
            log.error("Couldn't sleep", e);
        } finally {
            log.info("[{}] {}: {}", guild.getName(), member.getEffectiveName(), query);
        }
        PLAYER.loadItemOrdered(musicManager, query, new DefaultAudioLoadResultHandler(musicManager, member));
    }

    public static void previousTrack(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .map(GuildMusicManager::getScheduler)
                .ifPresent(TrackScheduler::previousTrack);
    }

    public static void pause(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .map(GuildMusicManager::getPlayer)
                .ifPresent(p -> p.setPaused(!p.isPaused()));
    }

    public static void skip(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .map(GuildMusicManager::getScheduler)
                .ifPresent(TrackScheduler::nextTrack);
    }

    public static void stop(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .ifPresent(m -> {
                    m.getPlayer().stopTrack();
                    m.getScheduler().clear();
                });
    }

    public static void loop(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .map(GuildMusicManager::getScheduler)
                .ifPresent(TrackScheduler::toggleLooping);
    }

    public static void loopPlaylist(Member member) throws BotException {
        requireSameVoiceChannel(member);

        Optional.ofNullable(MANAGERS.get(member.getGuild().getIdLong()))
                .map(GuildMusicManager::getScheduler)
                .ifPresent(TrackScheduler::toggleLoopingPlaylist);
    }

    public static void leave(Member member) throws BotException {
        stop(member);
        disconnectFromVoiceChannel(member.getGuild());
    }

    public static void connectToVoiceChannel(VoiceChannel channel) {
        final AudioManager audioManager = channel.getGuild().getAudioManager();

        if (audioManager.isConnected()) return;
        audioManager.openAudioConnection(channel);
    }

    public static void disconnectFromVoiceChannel(Guild guild) {
        final AudioManager audioManager = guild.getAudioManager();
        if (!audioManager.isConnected()) return;
        log.info("[{}] Disconnecting from {}", guild.getName(), Optional.ofNullable(audioManager.getConnectedChannel())
                .map(Channel::getName)
                .orElse("voice channel"));
        audioManager.closeAudioConnection();
    }

    public static Collection<GuildMusicManager> getManagers() {
        return MANAGERS.values();
    }

    private static VoiceChannel retrieveMemberVoiceChannel(Member member) throws VoiceChannelRequiredException {
        return Optional.ofNullable(member.getVoiceState())
                .map(GuildVoiceState::getChannel)
                .map(AudioChannelUnion::asVoiceChannel)
                .orElseThrow(VoiceChannelRequiredException::new);
    }

    private static void requireSameVoiceChannel(Member member) throws BotException {
        final VoiceChannel voice = retrieveMemberVoiceChannel(member);
        final AudioManager audioManager = member.getGuild().getAudioManager();

        if (!audioManager.isConnected() || !Objects.equals(audioManager.getConnectedChannel(), voice))
            throw new BotException("You should be in a voice channel with the bot");
    }

    private static GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MANAGERS.computeIfAbsent(guildId, id -> new GuildMusicManager(guild, PLAYER));
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

}
