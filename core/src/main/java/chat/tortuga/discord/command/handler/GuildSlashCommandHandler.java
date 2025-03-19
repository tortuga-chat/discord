package chat.tortuga.discord.command.handler;

import chat.tortuga.discord.exception.BotException;
import chat.tortuga.discord.exception.GuildRequiredException;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class GuildSlashCommandHandler extends SlashCommandHandler {

    protected Guild guild;

    protected void validate() throws BotException {
        if (Objects.isNull(event.getGuild()))
            throw new GuildRequiredException();

        this.guild = event.getGuild();
    }

}
