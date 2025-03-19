package chat.tortuga.discord;

import chat.tortuga.discord.command.CommandLoader;
import chat.tortuga.discord.command.handler.SlashCommandHandler;
import chat.tortuga.discord.task.TaskLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.util.Objects;
import java.util.stream.Collectors;

import static chat.tortuga.discord.config.ConfigLoader.CORE;

@Slf4j
@Getter
@SuppressWarnings("unused")
public class DiscordBot {

    protected final JDA jda;
    protected final EventListener listener;

    protected DiscordBot() {
        this(JDABuilder.createLight(CORE.getDiscord().getToken()));
    }

    protected DiscordBot(JDABuilder builder) {
        this(new EventListener(), builder);
    }

    protected DiscordBot(EventListener listener, JDABuilder builder) {
        this.listener = listener;
        this.jda = builder.addEventListeners(listener).build();
    }

    public void registerCommands() {
        jda.updateCommands()
                .addCommands(CommandLoader.getSlashHandlers()
                        .stream()
                        .map(DiscordBot::getInstanceOf)
                        .filter(Objects::nonNull)
                        .map(SlashCommandHandler::build)
                        .collect(Collectors.toSet()))
                .queue();
    }

    public void shutdown() {
        TaskLoader.getInstance().shutdown();
        listener.shutdown();
        jda.shutdown();
    }

    private static <T> T getInstanceOf(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Error instantiating class {}", clazz.getName(), e);
            return null;
        }
    }
}
