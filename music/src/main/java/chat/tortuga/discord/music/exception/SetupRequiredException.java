package chat.tortuga.discord.music.exception;

import chat.tortuga.discord.core.exception.BotException;

public class SetupRequiredException extends BotException {

    public SetupRequiredException() {
        super("Setup is required! Ask an admin to run /setup");
    }

}
