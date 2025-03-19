package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.TortugaCommandHandler;
import chat.tortuga.discord.exception.BotException;
import chat.tortuga.discord.service.music.MusicService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

@Command(name = "leave", description = "Leaves the voice channel")
public class Leave extends TortugaCommandHandler {

    @Override
    protected void handle() throws BotException {
        MusicService.leave(event.getMember());
        event.getInteraction()
                .reply(MessageCreateData.fromEmbeds(
                        new EmbedBuilder()
                                .setTitle("🍂 Bye")
                                .setColor(Color.green)
                                .build()))
                .setEphemeral(true)
                .queue();
    }

}
