package chat.tortuga.discord.exception;

public class VoiceChannelRequiredException extends BotException {

    public VoiceChannelRequiredException() {
        super("Voice channel is required");
    }

}
