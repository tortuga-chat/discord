package chat.tortuga.discord.jellyfin.service.listener;

import chat.tortuga.discord.jellyfin.config.Jellyfin;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MessageReactionListener extends ListenerAdapter {

    private final Jellyfin jellyfin;

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot() ||
                !Objects.equals(jellyfin.channelId(), event.getChannel().getIdLong())) return;

        event.getChannel()
                .deleteMessageById(event.getMessageIdLong())
                .queue();
    }
}
