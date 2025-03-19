package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.TortugaCommandHandler;
import chat.tortuga.discord.service.SingleMessageService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

@Command(name = "clear", description = "Clears the channel", permissions = Permission.ADMINISTRATOR)
public class Clear extends TortugaCommandHandler {

    @Override
    protected void handle() {
        SingleMessageService.cleanChannel(guild);
        event.reply(MessageCreateData.fromEmbeds(
                new EmbedBuilder()
                        .setTitle("Channel cleared")
                        .setColor(Color.green)
                        .build()))
                .setEphemeral(true)
                .queue();
    }

}
