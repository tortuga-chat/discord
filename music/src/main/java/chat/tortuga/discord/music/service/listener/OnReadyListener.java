package chat.tortuga.discord.music.service.listener;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.config.Discord;
import chat.tortuga.discord.core.listener.BaseOnReadyListener;
import chat.tortuga.discord.music.service.playlist.message.GuildPlayerMessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ApplicationScoped
public class OnReadyListener extends BaseOnReadyListener {

    private final GuildPlayerMessageService messageService;

    public OnReadyListener(Discord discord, Instance<SlashCommand> slashCommands, GuildPlayerMessageService messageService) {
        super(discord, slashCommands);
        this.messageService = messageService;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        messageService.cleanUpManagedChannels();
    }

}
