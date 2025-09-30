package chat.tortuga.discord.core.listener;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.config.Discord;
import jakarta.enterprise.inject.Instance;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class BaseOnReadyListener extends ListenerAdapter {

    private Discord discord;
    private Instance<SlashCommand> slashCommands;

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
    }

}
