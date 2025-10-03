package chat.tortuga.discord.music.service.playlist.message;

import chat.tortuga.discord.core.DiscordBot;
import chat.tortuga.discord.core.OnBotReady;
import chat.tortuga.discord.music.persistence.model.GuildSettings;
import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.util.Optional;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GuildPlayerMessageService implements OnBotReady {

    private final DiscordBot bot;
    private final GuildSettingsRepository repository;

    @Override
    public void accept(ReadyEvent readyEvent) {
        cleanUpManagedChannels();
    }

    public void cleanUpManagedChannels() {
        log.debug("Cleaning managed channels...");
        for (final GuildSettings settings : repository.findAll()) {
            // retrieves guild from JDA
            Optional.ofNullable(bot.getJda().getGuildById(settings.getGuildId()))
                    // maps to text channel
                    .map(g -> g.getTextChannelById(settings.getMusicChannelId()))
                    .ifPresent(this::cleanUp);
        }
    }

    public void cleanUp(@Nonnull final TextChannel channel) {
        channel.getHistoryFromBeginning(100).queue(h -> {
            if (!h.getRetrievedHistory().isEmpty())
                channel.deleteMessages(h.getRetrievedHistory())
                        // logs success
                        .queue(s -> log.debug("[{}] Cleaned channel {}", channel.getGuild().getName(), channel.getName()));
        });
    }

}
