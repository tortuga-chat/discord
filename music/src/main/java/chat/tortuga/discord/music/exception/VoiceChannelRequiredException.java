package chat.tortuga.discord.music.exception;

import chat.tortuga.discord.core.exception.BotException;

public class VoiceChannelRequiredException extends BotException {

    public VoiceChannelRequiredException() {
        super("User should be connected to a voice channel");
    }
}
