package chat.tortuga.discord.music.service;

import chat.tortuga.discord.music.persistence.model.UserSettings;
import chat.tortuga.discord.music.service.playlist.GuildPlayer;
import chat.tortuga.discord.music.service.playlist.message.PlayerMessageInfo;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Slf4j
public class TrackLoadResultHandler implements AudioLoadResultHandler {

    protected final Guild guild;
    protected final Member member;
    protected final GuildPlayer manager;
    protected final UserSettings userSettings;
    protected final boolean asNext;
    protected final boolean playlistFromTrack;

    public TrackLoadResultHandler(GuildPlayer manager, Member member, UserSettings userSettings, boolean asNext, boolean playlistFromTrack) {
        this.guild = member.getGuild();
        this.member = member;
        this.manager = manager;
        this.userSettings = userSettings;
        this.asNext = asNext;
        this.playlistFromTrack = playlistFromTrack;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        manager.add(attachUserData(track), asNext);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            manager.add(attachUserData(playlist.getTracks().getFirst()), asNext);
            return;
        }
        List<AudioTrack> tracks = playlist.getTracks();
        if (playlist.getSelectedTrack() != null) {
            if (playlistFromTrack && !userSettings.shouldLoadPlaylistFromTrack()) {
                manager.add(attachUserData(playlist.getSelectedTrack()), asNext);
                return;
            }
            tracks = tracks.subList(tracks.indexOf(playlist.getSelectedTrack()), tracks.size());
        }
        manager.addAll(tracks.stream().map(this::attachUserData).toList(), asNext);
    }

    @Override
    public void noMatches() {
        log.warn("[{}] No matches found...", guild.getName());
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("[{}] Failed to load track", guild.getName(), exception);
    }

    protected AudioTrack attachUserData(AudioTrack track) {
        track.setUserData(new PlayerMessageInfo(track, member.getAsMention()));
        return track;
    }

}
