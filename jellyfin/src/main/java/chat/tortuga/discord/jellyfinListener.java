package chat.tortuga.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class jellyfinListener extends EventListener {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        log.info("Received reaction from user#{} - Deleting message#{}", event.getUserId(), event.getMessageId());
        try {
            event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
