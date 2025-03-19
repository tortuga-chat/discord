package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.SlashCommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

import static net.dv8tion.jda.api.interactions.InteractionContextType.BOT_DM;
import static net.dv8tion.jda.api.interactions.InteractionContextType.GUILD;

@Command(name = "ping", description = "Checks bot health", contexts = {GUILD, BOT_DM})
public class Ping extends SlashCommandHandler {

    @Override
    protected void handle() {
        event.reply(MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle("🏓 Pong!")
                                .setColor(Color.orange)
                                .build()))
                .setEphemeral(true)
                .queue();
    }

}
