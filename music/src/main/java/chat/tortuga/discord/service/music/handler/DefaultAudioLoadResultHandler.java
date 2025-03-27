package chat.tortuga.discord.service.music.handler;

import chat.tortuga.discord.service.music.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultAudioLoadResultHandler implements AudioLoadResultHandler {

    private final GuildMusicManager musicManager;
    private final Member member;
    private final String guild;

    public DefaultAudioLoadResultHandler(GuildMusicManager musicManager, Member member) {
        this.musicManager = musicManager;
        this.member = member;
        this.guild = member.getGuild().getName();
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        checkIfConnected();
        musicManager.getScheduler().add(member, track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        checkIfConnected();
        log.info("[{}] Loaded playlist {}", guild, playlist.getName());
        if (playlist.isSearchResult()) {
            musicManager.getScheduler().add(member, playlist.getTracks().getFirst());
            return;
        }
        musicManager.getScheduler().add(member, getTracksAfterSelectedTrack(playlist));
    }

    @Override
    public void noMatches() {
        log.info("[{}] Nothing found...", guild);
    }

    @Override
    public void loadFailed(FriendlyException e) {
        log.info("[{}] Load failed", guild, e);
    }

    private void checkIfConnected() {
        if (!member.getGuild().getAudioManager().isConnected())
            throw new IllegalStateException("Bot is not connected");
    }

    public static List<AudioTrack> getTracksAfterSelectedTrack(AudioPlaylist playlist) {
        List<AudioTrack> filtered = new ArrayList<>();
        List<AudioTrack> tracks = playlist.getTracks();
        AudioTrack selectedTrack = playlist.getSelectedTrack();

        final int index = selectedTrack != null ? tracks.indexOf(selectedTrack) : 0;
        for (int i = index; i < tracks.size(); i++) {
            AudioTrack track = tracks.get(i);
            filtered.add(track);
        }
        return filtered;
    }

}
