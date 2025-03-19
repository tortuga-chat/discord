package chat.tortuga.discord.service;

import chat.tortuga.discord.persistence.model.GuildSettings;
import chat.tortuga.discord.service.music.PlayerMessage;
import chat.tortuga.discord.service.music.TrackScheduler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SingleMessageService {

    private static final ConcurrentHashMap<Long, PlayerMessage> MESSAGES = new ConcurrentHashMap<>();

    public static PlayerMessage get(long guildId) {
        return MESSAGES.get(guildId);
    }

    public static void error(Guild guild, String description) {
        temporary(guild, new EmbedBuilder()
                .setTitle("Error!")
                .setDescription(description)
                .setColor(Color.red)
                .build());
    }

    public static void warn(Guild guild, String title) {
        temporary(guild, new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.yellow)
                .build());
    }

    public static void warn(Guild guild, String title, String description) {
        temporary(guild, new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.yellow)
                .build());
    }

    public static void temporary(Guild guild, MessageEmbed... embeds) {
        getMusicChannel(guild)
                .sendMessage(MessageCreateData.fromEmbeds(embeds))
                .queue(m -> {
                    try {
                        Thread.sleep(Duration.ofSeconds(30));
                    } catch (InterruptedException e) {
                        log.error("couldn't sleep", e);
                    } finally {
                        m.delete().queue();
                    }
                });
    }

    public static void update(Guild guild, TrackScheduler scheduler) {
        if (!scheduler.hasCurrentTrack()) {
            delete(guild);
            return;
        }
        final Long guildId = guild.getIdLong();
        final PlayerMessage message = MESSAGES.getOrDefault(guildId, new PlayerMessage(scheduler));
        final TextChannel channel = getMusicChannel(guild);

        if (message.getMessageId() == null)
            channel.sendMessage(message.get())
                    .queue(m -> {
                        log.trace("Created player message for {}", guild);
                        message.setMessageId(m.getIdLong());
                        MESSAGES.put(guildId, message);
                    });
        else
            channel.editMessageById(message.getMessageId(), message.getEdit())
                    .queue(m -> log.trace("Updated player message for {}", guild));

        if (message.getPlaylistMessageId() == null)
            channel.sendMessage(message.getPlaylist())
                    .queue(m -> {
                        log.trace("Created queue message for {}", guild);
                        message.setPlaylistMessageId(m.getIdLong());
                    });
        else
            channel.editMessageById(message.getPlaylistMessageId(), message.getPlaylistEdit())
                    .queue(m -> log.trace("Updated queue message for {}", guild));
    }

    public static void delete(Guild guild) {
        if (!MESSAGES.containsKey(guild.getIdLong())) return;

        final PlayerMessage message = MESSAGES.remove(guild.getIdLong());
        final TextChannel channel = getMusicChannel(guild);

        channel.deleteMessageById(message.getMessageId())
                .queue(s -> log.debug("Deleted player message for {}", guild));
        channel.deleteMessageById(message.getPlaylistMessageId())
                .queue(s -> log.debug("Deleted queue message for {}", guild));
    }

    public static boolean manages(Long messageId) {
        return MESSAGES.values().stream().anyMatch(p -> p.isManagingMessage(messageId));
    }

    public static void cleanUpChannels(JDA jda) {
        log.info("Cleaning up channels...");
        GuildSettingsService.findAll().forEach(guildSettings ->
                cleanChannel(Objects.requireNonNull(jda.getGuildById(guildSettings.getGuildId()))));
    }

    public static void cleanChannel(Guild guild) {
        final TextChannel channel = getMusicChannel(guild);
        channel.getHistoryFromBeginning(100).queue(s -> {
            if (s.size() > 1)
                channel.deleteMessages(s.getRetrievedHistory()).queue(v -> {
                    log.debug("Deleted all messages for {}#{}", guild.getName(), channel.getName());
                    MESSAGES.remove(guild.getIdLong());
                });
            else if (!s.isEmpty())
                channel.deleteMessageById(s.getRetrievedHistory().getFirst().getIdLong()).queue(v -> {
                    log.debug("Deleted message for {}#{}", guild.getName(), channel.getName());
                    MESSAGES.remove(guild.getIdLong());
                });
        });
    }

    protected static TextChannel getMusicChannel(Guild guild) {
        return GuildSettingsService.findById(guild.getIdLong())
                .map(GuildSettings::getMusicChannelId)
                .map(guild::getTextChannelById)
                .orElse(null);
    }

}
