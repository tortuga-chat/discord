package chat.tortuga.discord.core.command;

import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@Dependent
public class Ping extends SlashCommand {

    protected Ping() {
        super("ping", "Checks if bot is up");
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Pong!")
                        .build())
                .setEphemeral(true)
                .queue();
    }
}
