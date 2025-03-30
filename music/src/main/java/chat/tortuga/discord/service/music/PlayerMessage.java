package chat.tortuga.discord.service.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.SUCCESS;

@Slf4j
@Data
public class PlayerMessage {

    public static final String BUTTON_PREV = "player-prev";
    public static final String BUTTON_PAUSE = "player-pause";
    public static final String BUTTON_SKIP = "player-skip";
    public static final String BUTTON_STOP = "player-stop";
    public static final String BUTTON_LOOP = "player-loop";
    public static final String BUTTON_LOOP_PLAYLIST = "player-loop-playlist";

    private static final String EMOJI_PREV = "⏮";
    private static final String EMOJI_PAUSE = "⏸";
    private static final String EMOJI_SKIP = "⏭";
    private static final String EMOJI_STOP = "⏹";
    private static final String EMOJI_LOOP_ONCE = "🔂";
    private static final String EMOJI_LOOP = "🔁";
    private static final String EMOJI_SONG = "🎶";
    private static final String EMOJI_LIST = "📋";
    private static final String EMOJI_LIVE = "🔴";
    private static final String[] EMOJIS_DESERT = {"🌵", "🏝️", "🏜️"};
    private static final String EMOJI_TORTUGA = "<:tortuga:1350930965714698311>";
    private static final List<String> EMOJIS_AUDIO_SOURCES = List.of(
            "<:youtube:1350684626624184394>",
            "<:soundcloud:1350684790889775125>",
            "<:twitch:1350931157562298398>",
            "<:bandcamp:1350929589744046081>",
            "<:vimeo:1350931273803108495>");

    private static final int PLAYLIST_LIMIT = 20;

    private Long messageId;
    private Long playlistMessageId;
    private TrackScheduler scheduler;

    public PlayerMessage(TrackScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public MessageCreateData get() {
        return new MessageCreateBuilder()
                .addEmbeds(getEmbed().build())
                .addActionRow(
                        getPrevButton(),
                        getPauseButton(),
                        getSkipButton())
                .addActionRow(
                        getStopButton(),
                        getLoopButton(),
                        getLoopPlaylistButton())
                .build();
    }

    public MessageEditData getEdit() {
        return MessageEditData.fromCreateData(get());
    }

    public MessageCreateData getPlaylist() {
        return MessageCreateData.fromEmbeds(getPlaylistEmbed().build());
    }

    public MessageEditData getPlaylistEdit() {
        return MessageEditData.fromCreateData(getPlaylist());
    }

    public EmbedBuilder getEmbed() {
        final AudioTrack track = scheduler.getCurrentTrack();
        return new EmbedBuilder()
                .setTitle(String.format("%s %s",
                        scheduler.isLooping() ? EMOJI_LOOP_ONCE : EMOJI_SONG,
                        "Playing Now"))
                .setThumbnail(track.getInfo().artworkUrl)
                .addField(
                        "Track",
                        String.format("%s [**%s**](%s)",
                            EMOJIS_AUDIO_SOURCES
                                .stream()
                                .filter(e -> e.contains(track.getSourceManager().getSourceName()))
                                .findFirst()
                                .orElse(EMOJI_TORTUGA),
                            track.getInfo().title,
                            track.getInfo().uri),
                        true)
                .addField("Requested", track.getUserData(Member.class).getAsMention(), true)
                .addField("Duration", String.format("`%s`", getDurationFormatted(track.getDuration())), true)
                .setColor(Color.green);
    }

    public EmbedBuilder getPlaylistEmbed() {
        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle(String.format("%s Playlist", scheduler.isLoopingPlaylist() ? EMOJI_LOOP : EMOJI_LIST))
                .setColor(Color.green);
        final StringBuilder sb = new StringBuilder();

        if (scheduler.hasPreviousTrack())
            sb.append("⏮️")
                    .append(" ")
                    .append(scheduler.getPreviousTrack().getInfo().author)
                    .append(" - ")
                    .append(scheduler.getPreviousTrack().getInfo().title)
                    .append("\n");

        if (scheduler.isEmpty() && !scheduler.isLoopingPlaylist()) {
            sb.append(String.format("%s ...", EMOJIS_DESERT[new Random().nextInt(EMOJIS_DESERT.length)]));
            return builder.setDescription(sb.toString());
        }
        long totalTime = scheduler.getCurrentTrack().getDuration();
        sb.append("0. ").append(scheduler.getCurrentTrack().getInfo().title);

        for (int i = 0; i < scheduler.getPlaylist().size() && i < PLAYLIST_LIMIT; i++) {
            AudioTrack track = scheduler.getPlaylist().get(i);
            sb.append("\n").append(i+1).append(". ").append(track.getInfo().title);
            totalTime += track.getDuration();
        }
        if (scheduler.getPlaylist().size() > PLAYLIST_LIMIT) {
            sb.append("\n\nAnd **").append(scheduler.getPlaylist().size() - PLAYLIST_LIMIT).append("** more...");
        }
        return builder
                .setDescription(sb.toString())
                .setFooter(getDurationFormatted(totalTime));
    }

    public Button getButton(String id) {
        return switch (id) {
            case BUTTON_PREV -> getPrevButton();
            case BUTTON_PAUSE -> getPauseButton();
            case BUTTON_SKIP -> getSkipButton();
            case BUTTON_STOP -> getStopButton();
            case BUTTON_LOOP -> getLoopButton();
            case BUTTON_LOOP_PLAYLIST -> getLoopPlaylistButton();
            default -> {
                log.error("Button '{}' not found", id);
                yield null;
            }
        };
    }

    public Button getPrevButton() {
        return Button.primary(BUTTON_PREV, Emoji.fromUnicode(EMOJI_PREV));
    }

    public Button getPauseButton() {
        return Button.of(scheduler.isPaused() ? SUCCESS : PRIMARY, BUTTON_PAUSE, Emoji.fromUnicode(EMOJI_PAUSE));
    }

    public Button getSkipButton() {
        return Button.primary(BUTTON_SKIP, Emoji.fromUnicode(EMOJI_SKIP));
    }

    public Button getStopButton() {
        return Button.primary(BUTTON_STOP, Emoji.fromUnicode(EMOJI_STOP));
    }

    public Button getLoopButton() {
        return Button.of(scheduler.isLooping() ? SUCCESS : PRIMARY, BUTTON_LOOP, Emoji.fromUnicode(EMOJI_LOOP_ONCE));
    }

    public Button getLoopPlaylistButton() {
        return Button.of(scheduler.isLoopingPlaylist() ? SUCCESS : PRIMARY, BUTTON_LOOP_PLAYLIST, Emoji.fromUnicode(EMOJI_LOOP));
    }

    public boolean isManagingMessage(Long id) {
        return Objects.equals(messageId, id) || Objects.equals(playlistMessageId, id);
    }

    private static String getDurationFormatted(long millis) {
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
