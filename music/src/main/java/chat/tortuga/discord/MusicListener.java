package chat.tortuga.discord;

import chat.tortuga.discord.exception.BotException;
import chat.tortuga.discord.service.GuildSettingsService;
import chat.tortuga.discord.service.SingleMessageService;
import chat.tortuga.discord.service.music.MusicService;
import chat.tortuga.discord.service.music.PlayerMessage;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class MusicListener extends EventListener {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        SingleMessageService.cleanUpChannels(event.getJDA());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild() || GuildSettingsService.isChannelNotManaged(event.getChannel().getIdLong()))
            return;

        executor.execute(() -> {
            try {
                MusicService.enqueue(Objects.requireNonNull(event.getMember()), event.getMessage().getContentStripped());
            } catch (BotException e) {
                handleException(event.getGuild(), e);
            } catch (Exception e) {
                handleException(event.getGuild(), e);
            } finally {
                event.getMessage().delete().queue();
            }
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild() || event.getGuild() == null)
            return;

        executor.execute(() -> {
            boolean shouldAcknowledge = true;
            try {
                log.info("[{}] Button pressed: {}", event.getGuild().getName(), event.getComponentId());
                switch (event.getComponentId()) {
                    case PlayerMessage.BUTTON_PREV -> MusicService.previousTrack(event.getMember());
                    case PlayerMessage.BUTTON_PAUSE -> MusicService.pause(event.getMember());
                    case PlayerMessage.BUTTON_SKIP -> MusicService.skip(event.getMember());
                    case PlayerMessage.BUTTON_STOP -> MusicService.stop(event.getMember());
                    case PlayerMessage.BUTTON_LOOP -> MusicService.loop(event.getMember());
                    case PlayerMessage.BUTTON_LOOP_PLAYLIST -> MusicService.loopPlaylist(event.getMember());
                    default -> {
                        log.warn("[{}] Button {} not handled...", event.getGuild().getName(), event.getComponentId());
                        shouldAcknowledge = false;
                    }
                }
                if (shouldAcknowledge) {
//                    event.deferReply(true).queue();
                    final long guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
                    final PlayerMessage message = SingleMessageService.get(guildId);
                    // a lot of things could have happened between this event being received and now
                    if (message == null) return;

                    event.editButton(message.getButton(event.getComponentId()).asDisabled()).queue(s -> {
                        try {
                            Thread.sleep(Duration.ofSeconds(2));
                        } catch (InterruptedException e) {
                            log.error("Couldn't sleep", e);
                        } finally {
                            event.editButton(message.getButton(event.getComponentId()))
                                    .queue();
//                            event.getHook().deleteOriginal().queue();
                        }
                    });
                }
            } catch (BotException e) {
                handleException(event.getInteraction(), e);
            } catch (Exception e) {
                handleException(event.getInteraction(), e);
            }
        });
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        executor.execute(() -> {
            if (GuildSettingsService.isChannelNotManaged(event.getChannel().getIdLong()))
                return;
            if (SingleMessageService.manages(event.getMessageIdLong()))
                return;
            event.getChannel()
                    .deleteMessageById(event.getMessageIdLong())
                    .queue();
        });
    }

    protected void handleException(ButtonInteraction interaction, BotException e) {
        log.warn(e.getMessage());
        interaction.reply(e.getMessage()).queue();
    }

    protected void handleException(ButtonInteraction interaction, Exception e) {
        log.error("[{}] Error handling query", Objects.requireNonNull(interaction.getGuild()).getName(), e);
        interaction.reply(e.getMessage()).queue();
    }

    protected void handleException(Guild guild, BotException e) {
        log.warn(e.getMessage());
        SingleMessageService.warn(guild, e.getMessage());
    }

    protected void handleException(Guild guild, Exception e) {
        log.error("[{}] Error handling query", guild.getName(), e);
        SingleMessageService.error(guild, e.getMessage());
    }

}
