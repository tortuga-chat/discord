package chat.tortuga.discord.music.service.playlist.message;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.Random;

import static chat.tortuga.discord.music.util.TrackUtils.*;

@UtilityClass
public class PlaylistMessage {

    private final Random RANDOM = new Random();
    private final int PLAYLIST_LIMIT = 20;

    public MessageCreateData from(PlaylistMessageInfo info) {
        return MessageCreateData
                .fromEmbeds(embedFrom(info)
                .build());
    }

    public EmbedBuilder embedFrom(PlaylistMessageInfo info) {
        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle(String.format("%s Playlist", info.loopingPlaylist() ? EMOJI_LOOP : EMOJI_LIST))
                .setColor(Color.green);

        final StringBuilder sb = new StringBuilder();

        if (info.previous() != null)
            sb.append(String.format("%s %s %n", EMOJI_PREV, info.previous().title()));

        if (info.playlist().isEmpty()) {
            sb.append(String.format("%s ...", EMOJIS_DESERT[RANDOM.nextInt(EMOJIS_DESERT.length)]));
            return builder.setDescription(sb.toString());
        }
        // if playlist is not empty...
        final PlayerMessageInfo current = info.current();
        final long size = info.playlist().size();

        sb.append(String.format("0. %s", current.title()));
        long totalTime = current.duration();

        for (int i = 0; i < size && i < PLAYLIST_LIMIT; i++) {
            final PlayerMessageInfo track = info.playlist().get(i);
            sb.append(String.format("%n%d. %s", i+1, track.title()));
            totalTime += track.duration();
        }

        if (size > PLAYLIST_LIMIT) {
            sb.append(String.format("%n%nAnd **%d** more...", size - PLAYLIST_LIMIT));
        }
        return builder
                .setDescription(sb.toString())
                .setFooter(getDurationFormatted(totalTime));
    }
    
}
