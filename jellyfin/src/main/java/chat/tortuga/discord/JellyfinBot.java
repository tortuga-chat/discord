package chat.tortuga.discord;

import chat.tortuga.discord.config.ConfigLoader;
import chat.tortuga.discord.config.Discord;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.EnumSet;

import static chat.tortuga.discord.config.ConfigLoader.CORE;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.SCHEDULED_EVENTS;

@Slf4j
public class JellyfinBot extends DiscordBot {

    private static final EnumSet<GatewayIntent> INTENTS = EnumSet.of(GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_MESSAGE_REACTIONS);
    private static final Discord CONFIG = new ConfigLoader().load("config.yml", Discord.class);
    private static JellyfinBot instance;

    public static JellyfinBot getInstance() {
        if (instance == null) instance = new JellyfinBot();
        return instance;
    }

    private JellyfinBot() {
        super(new jellyfinListener(), JDABuilder.createDefault(CORE.getDiscord().getToken(), INTENTS)
                .disableCache(EMOJI, STICKER, SCHEDULED_EVENTS, VOICE_STATE));
    }

    public Message sendUpdateMessage(EmbedBuilder embed) {
        return getUpdatesChannel().sendMessage(MessageCreateData.fromEmbeds(embed.build())).complete();
    }

    public TextChannel getUpdatesChannel() {
        return getJda().getTextChannelById(CONFIG.getChannelId());
    }
}