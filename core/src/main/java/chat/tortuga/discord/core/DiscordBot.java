package chat.tortuga.discord.core;

import chat.tortuga.discord.core.config.Discord;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_AFTER;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DiscordBot {

    private final Discord discord;
    private final Instance<ListenerAdapter> listeners;

    @Getter
    protected JDA jda;

    @Startup(LIBRARY_AFTER)
    void connect() {
        jda = JDABuilder
                .createDefault(discord.token())
                .enableIntents(discord.intents().orElse(GatewayIntent.getIntents(GatewayIntent.DEFAULT)))
                .addEventListeners(listeners.stream().toArray())
                .setStatus(OnlineStatus.IDLE)
                .build();
    }

}
