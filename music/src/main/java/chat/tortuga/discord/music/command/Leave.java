package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.exception.GuildRequiredException;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

import static chat.tortuga.discord.music.util.TrackUtils.*;

@Dependent
public class Leave extends SlashCommand {

    private final MusicService service;

    protected Leave(MusicService service) {
        super("leave", "Leaves the voice channel");
        this.service = service;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) throws BotException {
        if (event.getGuild() == null) throw new GuildRequiredException();
        service.handleStop(event.getGuild(), event.getMember());
        service.disconnectFromVoiceChannel(event.getGuild());

        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle(String.format("%s Bye", EMOJI_LEAF))
                        .build())
                .setEphemeral(true)
                .queue();
    }

}
