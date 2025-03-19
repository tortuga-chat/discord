package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.TortugaCommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

@Command(name = "ping", description = "Checks bot health")
public class Ping extends TortugaCommandHandler {

    @Override
    protected void handle() {
        event.reply(MessageCreateData.fromEmbeds(
                new EmbedBuilder()
                        .setTitle("🏓 Pong!")
                        .setColor(Color.green)
                        .build()))
                .setEphemeral(true)
                .queue();
    }

}
