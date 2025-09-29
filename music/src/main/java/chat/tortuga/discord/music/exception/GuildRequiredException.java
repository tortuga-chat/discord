package chat.tortuga.discord.music.exception;

import chat.tortuga.discord.core.exception.BotException;

public class GuildRequiredException extends BotException {

    public GuildRequiredException() {
        super("Command should be called in a server");
    }
}
