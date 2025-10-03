package chat.tortuga.discord.core.listener;

import chat.tortuga.discord.core.command.SlashCommand;
import jakarta.enterprise.inject.Instance;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class BaseSlashCommandInteractionListener extends ListenerAdapter {

    private Instance<SlashCommand> slashCommands;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info("[{}] {} used /{}", Optional.ofNullable(event.getGuild()).map(Guild::getName).orElse("DM"), event.getUser().getName(), event.getName());
        slashCommands.stream()
                .filter(c -> c.getName().equals(event.getName()))
                .findFirst()
                .ifPresentOrElse(c -> c.accept(event), () -> log.error("Slash command {} not found...", event.getName()));
    }

}
