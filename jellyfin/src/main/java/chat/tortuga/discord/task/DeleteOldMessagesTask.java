package chat.tortuga.discord.task;

import chat.tortuga.discord.JellyfinBot;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.OffsetDateTime;

@Slf4j
@Task(period = "2", unit = "DAYS")
public class DeleteOldMessagesTask implements Runnable {

    @Override
    public void run() {
        TextChannel channel = JellyfinBot.getInstance().getUpdatesChannel();
        log.info("Finding old messages to delete...");
        channel.deleteMessages(
                channel.getIterableHistory()
                        .stream()
                        .filter(m -> OffsetDateTime.now().isAfter(m.getTimeCreated().plusDays(2)))
                        .toList())
                .queue();
    }

}
