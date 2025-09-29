package chat.tortuga.discord.music.exception;

import chat.tortuga.discord.core.exception.BotException;

public class SameVoiceChannelRequiredException extends BotException {

    public SameVoiceChannelRequiredException() {
        super("Should be in a voice channel with the bot");
    }
}
