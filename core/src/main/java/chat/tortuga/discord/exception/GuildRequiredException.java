package chat.tortuga.discord.exception;

public class GuildRequiredException extends BotException {

    public GuildRequiredException() {
        super("This command should only be used in a guild");
    }

}
