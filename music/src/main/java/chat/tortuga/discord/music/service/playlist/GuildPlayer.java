package chat.tortuga.discord.music.service.playlist;

import chat.tortuga.discord.music.exception.SetupRequiredException;
import chat.tortuga.discord.music.service.playlist.message.PlayerMessage;
import chat.tortuga.discord.music.service.playlist.message.PlayerMessageInfo;
import chat.tortuga.discord.music.service.playlist.message.PlaylistMessage;
import chat.tortuga.discord.music.service.playlist.message.PlaylistMessageInfo;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Slf4j
@Getter
public class GuildPlayer extends AudioEventAdapter {

    private final Guild guild;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> playlist;
    private final Long channelId;

    private AudioTrack previousTrack;
    private boolean looping;
    private boolean loopingPlaylist;

    private Long playerMessageId;
    private Long playlistMessageId;

    private Instant lastEndOfPlaylist;

    public GuildPlayer(Guild guild, AudioPlayerManager manager, Long channelId) {
        this.guild = guild;
        this.player = manager.createPlayer();
        this.channelId = channelId;
        this.player.addListener(this);
        this.playlist = new LinkedBlockingQueue<>();
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
    }

    /**
     * Add the next track to the playlist or play right away if playlist is empty.
     *
     * @param track The track to play or add to the playlist
     */
    public void add(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            playlist.add(track);
            if (player.getPlayingTrack() != null)
                updatePlaylistMessage();
        }
    }

    /**
     * Adds all the tracks to the playlist and play right away if playlist is empty.
     * @param tracks Tracks to add
     */
    public void addAll(List<AudioTrack> tracks) {
        playlist.addAll(tracks);
        player.startTrack(playlist.poll(), true);
    }

    /**
     * Clears the playlist and stops the player.
     */
    public void stop() {
        log.debug("[{}] Stopping player", guild.getName());
        playlist.clear();
        player.stopTrack();
    }

    /**
     * Toggles whether the player is paused.
     */
    public void togglePause() {
        if (player.isPaused()) resume();
        else pause();
    }

    /**
     * Pauses the player.
     */
    public void pause() {
        player.setPaused(true);
    }

    /**
     * Resumes the player.
     */
    public void resume() {
        player.setPaused(false);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        player.startTrack(playlist.poll(), false);
    }

    /**
     * Start playing the previous track, stopping the current one.
     */
    public void previousTrack() {
        player.startTrack(previousTrack.makeClone(), false);
    }

    /**
     * Flag indicating to loop the current playing track.
     */
    public void toggleLooping() {
        looping = !looping;
        updatePlayerMessage();
    }

    /**
     * Flag indicating to loop the current playlist.
     */
    public void toggleLoopingPlaylist() {
        loopingPlaylist = !loopingPlaylist;
        updatePlaylistMessage();
    }

    public boolean isMessageManaged(long messageId) {
        return (playerMessageId != null && messageId == playerMessageId) ||
               (playlistMessageId != null && messageId == playlistMessageId);
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        log.info("[{}] player paused", guild.getName());
        updatePlayerMessage();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        log.info("[{}] player resumed",  guild.getName());
        updatePlayerMessage();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.info("[{}] playing '{}' ({}@{})", guild.getName(), track.getInfo().title, track.getIdentifier(), track.getSourceManager().getSourceName());
        updatePlayerMessage();
        updatePlaylistMessage();
        lastEndOfPlaylist = null;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        previousTrack = track;

        if (looping) {
            player.startTrack(track.makeClone(), true);
            return;
        }
        if (loopingPlaylist) {
            playlist.add(track.makeClone());
        }
        if (playlist.isEmpty() && player.getPlayingTrack() == null) {
            lastEndOfPlaylist = Instant.now();
            clearMessages();
            return;
        }
        if (endReason.mayStartNext) {
            nextTrack();
        } else {
            updatePlayerMessage();
            updatePlaylistMessage();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("[{}] Error playing {} ({}@{})", guild.getName(), track.getInfo().title,
                track.getIdentifier(), track.getSourceManager().getSourceName(), exception);
    }

    public void updatePlayerMessage() {
        final PlayerMessageInfo info = player.getPlayingTrack().getUserData(PlayerMessageInfo.class);
        final MessageCreateData createData = PlayerMessage.from(info, player.isPaused(), looping, loopingPlaylist);
        updateMessage(createData, playerMessageId, m -> playerMessageId = m.getIdLong());
    }

    public void updatePlaylistMessage() {
        final PlayerMessageInfo previous = previousTrack == null ? null : previousTrack.getUserData(PlayerMessageInfo.class);
        final PlayerMessageInfo current = player.getPlayingTrack().getUserData(PlayerMessageInfo.class);
        final List<PlayerMessageInfo> list = playlist.stream().map(t -> t.getUserData(PlayerMessageInfo.class)).toList();

        final PlaylistMessageInfo info = new PlaylistMessageInfo(previous, current, list, loopingPlaylist);
        final MessageCreateData createData = PlaylistMessage.from(info);
        updateMessage(createData, playlistMessageId, m -> playlistMessageId = m.getIdLong());
    }

    private void updateMessage(MessageCreateData createData, Long tracker, Consumer<Message> consumer) {
        try {
            if (tracker == null) {
                getChannel().sendMessage(createData).queue(consumer);
            } else {
                getChannel().editMessageById(tracker, MessageEditData.fromCreateData(createData)).queue();
            }
        } catch (SetupRequiredException e) {
            log.warn("[{}] failed to update message: {}", guild.getName(), e.getMessage());
        }
    }

    private void clearMessages() {
        try {
            getChannel()
                    .deleteMessagesByIds(List.of(String.valueOf(playerMessageId), String.valueOf(playlistMessageId)))
                    .queue(s -> {
                        playerMessageId = null;
                        playlistMessageId = null;
                    });
        } catch (SetupRequiredException e) {
            log.warn("[{}] failed to clear messages: {}", guild.getName(), e.getMessage());
        }
    }

    private TextChannel getChannel() throws SetupRequiredException {
        return Optional.ofNullable(guild.getTextChannelById(channelId)).orElseThrow(SetupRequiredException::new);
    }

}
