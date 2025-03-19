package chat.tortuga.discord.exception;

public class BotException extends Exception {

    public BotException(String message) {
        super(message);
    }

    public BotException(Throwable cause) {
        super(cause);
    }

    public BotException(String message, Throwable cause) {
        super(message, cause);
    }

}
