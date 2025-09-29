package chat.tortuga.discord.music.service.playlist.message;

import chat.tortuga.discord.core.DiscordBot;
import chat.tortuga.discord.core.OnBotReady;
import chat.tortuga.discord.music.persistence.model.GuildSettings;
import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                    // if successfully retrieved text channel, retrieve message history from the beginning
                    .ifPresent(c -> c.getHistoryFromBeginning(100)
                            // delete retrieved message history from channel
                            .queue(h -> {
                                if (!h.getRetrievedHistory().isEmpty())
                                    c.deleteMessages(h.getRetrievedHistory())
                                        // logs success
                                        .queue(s -> log.debug("[{}] Cleaned dedicated channel {}", c.getGuild().getName(), c.getName()));
                            }));
        }
    }

}
