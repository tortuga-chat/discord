package chat.tortuga.discord.music.service.playlist.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public record PlayerMessageInfo(String title, String author, String url, String artworkUrl, String identifier,
                                String source, String requester, long duration) {

    public PlayerMessageInfo(AudioTrack track, String requester) {
        this(track.getInfo().title, track.getInfo().author, track.getInfo().uri, track.getInfo().artworkUrl,
                track.getIdentifier(), track.getSourceManager().getSourceName(), requester, track.getDuration());
    }

    public PlayerMessageInfo(AudioTrack track) {
        this(track.getInfo().title, track.getInfo().author, track.getInfo().uri, track.getInfo().artworkUrl,
                track.getIdentifier(), track.getSourceManager().getSourceName(), null, track.getDuration());
    }

}
