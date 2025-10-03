package chat.tortuga.discord.music.service.playlist.message;

import chat.tortuga.discord.music.util.TrackUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.Optional;

import static chat.tortuga.discord.music.util.TrackUtils.*;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.SUCCESS;

@Slf4j
@UtilityClass
public class PlayerMessage {

    public final String BUTTON_PREV = "player-prev";
    public final String BUTTON_PAUSE = "player-pause";
    public final String BUTTON_SKIP = "player-skip";
    public final String BUTTON_STOP = "player-stop";
    public final String BUTTON_LOOP = "player-loop";
    public final String BUTTON_LOOP_PLAYLIST = "player-loop-playlist";

    public MessageCreateData from(final PlayerMessageInfo info, boolean paused, boolean looping, boolean loopingPlaylist) {
        return new MessageCreateBuilder()
                .addEmbeds(embedFrom(info, looping).build())
                .addActionRow(
                        getPrevButton(),
                        getPauseButton(paused),
                        getSkipButton())
                .addActionRow(
                        getStopButton(),
                        getLoopButton(looping),
                        getLoopPlaylistButton(loopingPlaylist))
                .build();
    }

    public EmbedBuilder embedFrom(final PlayerMessageInfo info, final boolean looping) {
        final String source = Optional.ofNullable(EMOJIS_AUDIO_SOURCES.get(info.source())).orElse(EMOJI_TORTUGA);
        final String track = String.format("%s [**%s**](%s)", source, info.title(), info.url());
        final String duration = String.format("`%s`", TrackUtils.getDurationFormatted(info.duration()));

        return new EmbedBuilder()
                .setTitle(String.format("%s %s", looping ? EMOJI_LOOP_ONCE : EMOJI_SONG, "Playing Now"))
                .setThumbnail(info.artworkUrl())
                .addField("Track", track, true)
                .addField("Requested", info.requester(), true)
                .addField("Duration", duration, true)
                .setColor(Color.GREEN);
    }

    public Button getButton(String id, boolean paused, boolean looping, boolean loopingPlaylist) {
        return switch (id) {
            case BUTTON_PREV -> getPrevButton();
            case BUTTON_PAUSE -> getPauseButton(paused);
            case BUTTON_SKIP -> getSkipButton();
            case BUTTON_STOP -> getStopButton();
            case BUTTON_LOOP -> getLoopButton(looping);
            case BUTTON_LOOP_PLAYLIST -> getLoopPlaylistButton(loopingPlaylist);
            default -> {
                log.error("Button '{}' not found", id);
                yield null;
            }
        };
    }

    public Button getPrevButton() {
        return Button.primary(BUTTON_PREV, Emoji.fromUnicode(EMOJI_PREV));
    }

    public Button getPauseButton(boolean paused) {
        return Button.of(paused ? SUCCESS : PRIMARY, BUTTON_PAUSE, Emoji.fromUnicode(EMOJI_PAUSE));
    }

    public Button getSkipButton() {
        return Button.primary(BUTTON_SKIP, Emoji.fromUnicode(EMOJI_SKIP));
    }

    public Button getStopButton() {
        return Button.primary(BUTTON_STOP, Emoji.fromUnicode(EMOJI_STOP));
    }

    public Button getLoopButton(boolean looping) {
        return Button.of(looping ? SUCCESS : PRIMARY, BUTTON_LOOP, Emoji.fromUnicode(EMOJI_LOOP_ONCE));
    }

    public Button getLoopPlaylistButton(boolean loopingPlaylist) {
        return Button.of(loopingPlaylist ? SUCCESS : PRIMARY, BUTTON_LOOP_PLAYLIST, Emoji.fromUnicode(EMOJI_LOOP));
    }

}
