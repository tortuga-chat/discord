package chat.tortuga.discord;

import chat.tortuga.discord.task.TaskLoader;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Arrays;
import java.util.EnumSet;

import static chat.tortuga.discord.config.ConfigLoader.CORE;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Slf4j
public class MusicBot extends DiscordBot {

    private static final EnumSet<GatewayIntent> INTENTS = EnumSet.of(GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS);

    public static void main(String[] args) {
        MusicBot bot = new MusicBot();

        if (Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("register"))) {
            bot.registerCommands();
            bot.shutdown();
        }
        TaskLoader.getInstance().start();
    }

    private MusicBot() {
        super(new MusicListener(), JDABuilder.createDefault(CORE.getDiscord().getToken(), INTENTS)
                .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS));
    }

}