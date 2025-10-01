package chat.tortuga.discord.jellyfin.service.listener;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.config.Discord;
import chat.tortuga.discord.core.listener.BaseOnReadyListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OnReadyListener extends BaseOnReadyListener {

    public OnReadyListener(Discord discord, Instance<SlashCommand> slashCommands) {
        super(discord, slashCommands);
    }

}
