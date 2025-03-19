package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.SlashCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandLoader {

    private static final Reflections REFLECTIONS = new Reflections("chat.tortuga.discord");
    private static final Map<String, Class<? extends SlashCommandHandler>> SLASH_HANDLERS = new ConcurrentHashMap<>();

    static {
        log.debug("Populating command handlers cache...");

        REFLECTIONS.getTypesAnnotatedWith(Command.class).forEach(handler -> {
            var command = handler.getAnnotation(Command.class);

            if (SlashCommandHandler.class.isAssignableFrom(handler)) {
                log.debug("Assigning {} to handle /{}", handler.getName(), command.name());
                SLASH_HANDLERS.put(command.name(), handler.asSubclass(SlashCommandHandler.class));
            }
        });
        log.info("Successfully loaded command handlers");
    }

    private CommandLoader() {}

    public static Optional<Class<? extends SlashCommandHandler>> getHandlerForSlash(String command) {
        return Optional.ofNullable(SLASH_HANDLERS.get(command));
    }

    public static Collection<Class<? extends SlashCommandHandler>> getSlashHandlers() {
        return SLASH_HANDLERS.values();
    }

}
