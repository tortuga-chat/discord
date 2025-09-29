package chat.tortuga.discord.music.service.listener;

import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.service.MusicService;
import chat.tortuga.discord.music.service.playlist.message.PlayerMessage;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ButtonInteractionEventListener extends ListenerAdapter {

    private final MusicService service;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild() || event.getGuild() == null)
            return;

        final Guild guild = event.getGuild();
        final Member member = event.getMember();

        try {
            log.debug("[{}] Button pressed: {}", guild.getName(), event.getComponentId());
            switch (event.getComponentId()) {
                case PlayerMessage.BUTTON_PAUSE -> service.handlePause(guild, member);
                case PlayerMessage.BUTTON_SKIP -> service.handleSkip(guild, member);
                case PlayerMessage.BUTTON_PREV -> service.handlePrevious(guild, member);
                case PlayerMessage.BUTTON_STOP -> service.handleStop(guild, member);
                case PlayerMessage.BUTTON_LOOP -> service.handleLooping(guild, member);
                case PlayerMessage.BUTTON_LOOP_PLAYLIST -> service.handleLoopingPlaylist(guild, member);
                default -> {
                    log.warn("[{}] Button {} not handled...", guild.getName(), event.getComponentId());
                    return;
                }
            }
            if(!service.isMessageManaged(guild, event.getMessageIdLong()))
                return;

            event.editButton(service.getButtonWithCurrentState(guild, event.getComponentId()).asDisabled()).queue(s -> {
                try {
                    Thread.sleep(Duration.ofSeconds(2));
                } catch (InterruptedException e) {
                    log.error("Couldn't sleep!", e);
                    Thread.currentThread().interrupt();
                } finally {
                    Button button = service.getButtonWithCurrentState(guild, event.getComponentId());
                    if (button != null) event.editButton(button).queue();
                }
            });
        } catch (BotException e) {
            handleException(event.getInteraction(), e);
        } catch (Exception e) {
            handleException(event.getInteraction(), e);
        }
    }

    protected void handleException(ButtonInteraction interaction, BotException e) {
        log.warn(e.getMessage());
        interaction.reply(MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle(e.getMessage())
                                .setColor(Color.yellow)
                                .build()))
                .setEphemeral(true)
                .queue();
    }

    protected void handleException(ButtonInteraction interaction, Exception e) {
        log.error("[{}] Error handling query", Objects.requireNonNull(interaction.getGuild()).getName(), e);
        interaction.reply(MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle(e.getMessage())
                                .setColor(Color.red)
                                .build()))
                .setEphemeral(true)
                .queue();
    }

}
