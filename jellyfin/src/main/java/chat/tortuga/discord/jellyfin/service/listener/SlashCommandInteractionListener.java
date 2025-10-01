package chat.tortuga.discord.jellyfin.service.listener;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.listener.BaseSlashCommandInteractionListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

@ApplicationScoped
public class SlashCommandInteractionListener extends BaseSlashCommandInteractionListener {

    public SlashCommandInteractionListener(Instance<SlashCommand> slashCommands) {
        super(slashCommands);
    }

}
