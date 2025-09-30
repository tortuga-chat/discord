package chat.tortuga.discord.music.service.listener;

import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MessageReactionListener extends ListenerAdapter {

    private final MusicService service;
    private final GuildSettingsRepository repository;

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild() ||
                repository.isNotDedicatedChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong()) ||
                service.isMessageManaged(event.getGuild(), event.getMessageIdLong()))
            return;

        event.getChannel()
                .deleteMessageById(event.getMessageIdLong())
                .queue();
    }
}
