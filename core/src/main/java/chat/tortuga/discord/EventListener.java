package chat.tortuga.discord;

import chat.tortuga.discord.command.CommandLoader;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventListener extends ListenerAdapter {

    protected final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Bot is ready!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CommandLoader.getHandlerForSlash(event.getName())
                        .ifPresent(h -> Objects.requireNonNull(getInstanceOf(h)).accept(event));
    }

    public void shutdown() {
        try {
            log.info("executor shutdown: {}", executor.awaitTermination(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            log.error("Failed to shutdown executor", e);
        }
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
