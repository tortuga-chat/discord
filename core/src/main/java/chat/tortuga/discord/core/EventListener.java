package chat.tortuga.discord.core;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.config.Discord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EventListener extends ListenerAdapter {

    private final Discord discord;
    private final Instance<SlashCommand> slashCommands;
    private final Instance<OnBotReady> onBotReady;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);

        if (discord.updateGlobalCommands().orElse(false)) {
            log.info("Updating global application commands: {}. To disable, set discord.update-global-commands to false.",
                    slashCommands.stream().map(SlashCommand::getName).toList());

            event.getJDA().updateCommands()
                    .addCommands(slashCommands.stream()
                            .map(SlashCommand::build)
                            .toList())
                    .queue();
        }
        onBotReady.forEach(i -> i.accept(event));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        slashCommands.stream()
                .filter(c -> c.getName().equals(event.getName()))
                .findFirst()
                .ifPresentOrElse(c -> c.accept(event), () -> log.error("Slash command {} not found...", event.getName()));
    }
}
