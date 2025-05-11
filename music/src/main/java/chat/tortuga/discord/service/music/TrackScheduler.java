package chat.tortuga.discord.service.music;

import chat.tortuga.discord.service.SingleMessageService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static chat.tortuga.discord.service.SingleMessageService.update;

@Slf4j
public class TrackScheduler extends AudioEventAdapter {

    private final Guild guild;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> playlist;

    @Getter
    private Instant endedAt;
    @Getter
    private AudioTrack previousTrack;
    @Getter
    private boolean looping;
    @Getter
    private boolean loopingPlaylist;

    public TrackScheduler(Guild guild, AudioPlayer player) {
        this.guild = guild;
        this.player = player;
        this.playlist = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the playlist.
     *
     * @param track The track to play or add to playlist.
     */
    public void add(Member requester, AudioTrack track) {
        track.setUserData(requester);
        if (!player.startTrack(track, true)) {
            //noinspection ResultOfMethodCallIgnored
            playlist.offer(track);
            update(guild, this);
        }
    }

    public void add(Member requester, List<AudioTrack> tracks) {
        playlist.addAll(tracks.stream().peek(t -> t.setUserData(requester)).toList());
        if (hasCurrentTrack())
            update(guild, this);
        else
            nextTrack();
    }

    public void previousTrack() {
        log.info("[{}] Previous track: {}", guild.getName(), previousTrack);
        if (hasPreviousTrack())
            player.startTrack(previousTrack.makeClone(), false);
        else if (hasCurrentTrack())
            player.startTrack(getCurrentTrack().makeClone(), false);
    }

    public void nextTrack() {
        previousTrack = player.getPlayingTrack();
        player.startTrack(playlist.poll(), false);
    }

    public void clear() {
        playlist.clear();
        update(guild, this);
    }

    public void toggleLooping() {
        looping = !looping;
        log.info("[{}] Looping toggled to '{}'", guild.getName(), looping);
        update(guild, this);
    }

    public void toggleLoopingPlaylist() {
        loopingPlaylist = !loopingPlaylist;
        log.info("[{}] Looping playlist toggled to '{}'", guild.getName(), loopingPlaylist);
        update(guild, this);
    }

    public AudioTrack getCurrentTrack () {
        return player.getPlayingTrack();
    }

    public boolean hasCurrentTrack() {
        return getCurrentTrack() != null;
    }

    public boolean hasPreviousTrack() {
        return previousTrack != null;
    }

    public List<AudioTrack> getPlaylist() {
        return playlist.stream().toList();
    }

    public boolean isEmpty() {
        return playlist.isEmpty();
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.info("[{}] Playing {} from {}", guild.getName(), track.getInfo().title, track.getSourceManager().getSourceName());
        update(guild, this);
        endedAt = null;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (looping) {
            player.startTrack(track.makeClone(), true);
            return;
        }
        if (loopingPlaylist)
            playlist.add(track.makeClone());

        if (isEmpty() && !hasCurrentTrack()) {
            endedAt = Instant.now();
            SingleMessageService.delete(guild);
            return;
        }
        if (endReason.mayStartNext) {
            previousTrack = track;
            nextTrack();
        } else {
            update(guild, this);
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        log.info("[{}] Player paused", guild.getName());
        update(guild, this);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        log.info("[{}] Player resumed", guild.getName());
        update(guild, this);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("[{}] Error playing track", guild.getName(), exception);
    }

}
