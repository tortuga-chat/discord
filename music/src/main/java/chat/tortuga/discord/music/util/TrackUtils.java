package chat.tortuga.discord.music.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.Map;

@UtilityClass
public class TrackUtils {

    public final String EMOJI_PREV = "⏮️";
    public final String EMOJI_PAUSE = "⏸";
    public final String EMOJI_SKIP = "⏭";
    public final String EMOJI_STOP = "⏹";
    public final String EMOJI_LOOP_ONCE = "🔂";
    public final String EMOJI_LOOP = "🔁";
    public final String EMOJI_SONG = "🎶";
    public final String EMOJI_LIVE = "🔴";
    public final String EMOJI_LIST = "📋";
    public final String EMOJI_LEAF ="🍂";
    public final String EMOJI_STARS = "✨";
    public final String EMOJI_TORTUGA = "<:tortuga:1350930965714698311>";
    public final String[] EMOJIS_DESERT = {"🌵", "🏝️", "🏜️"};
    public final Map<String, String> EMOJIS_AUDIO_SOURCES = Map.of(
            "youtube", "<:youtube:1350684626624184394>",
            "soundcloud", "<:soundcloud:1350684790889775125>",
            "twitch", "<:twitch:1350931157562298398>",
            "bandcamp", "<:bandcamp:1350929589744046081>",
            "vimeo", "<:vimeo:1350931273803108495>"
    );

    public static String getDurationFormatted(long millis) {
        if (Long.MAX_VALUE == millis)
            return String.format("%s Live", EMOJI_LIVE);

        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return hours > 0 ?
                String.format("%d:%02d:%02d", hours, minutes, seconds) :
                String.format("%02d:%02d", minutes, seconds);
    }

}
