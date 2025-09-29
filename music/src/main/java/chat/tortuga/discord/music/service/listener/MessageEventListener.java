package chat.tortuga.discord.music.service.listener;

import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MessageEventListener extends ListenerAdapter {

    private final MusicService service;
    private final GuildSettingsRepository settingsRepository;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild())
            return;

        final Guild guild = event.getGuild();
        final String guildName = event.getGuild().getName();

        if (settingsRepository.isNotDedicatedChannel(guild.getIdLong(), event.getChannel().getIdLong()))
            return;

        final User author = event.getAuthor();
        final String authorName = author.getName();
        final String query = event.getMessage().getContentStripped();

        try {
            service.handleUserQuery(guild, event.getChannel().getIdLong(), event.getMember(), query);
        } catch (BotException e) {
            log.warn("[{}] {}", guildName, e.getMessage());
        } catch (Exception e) {
            log.error("[{}] {}", guildName, e.getMessage(), e);
        } finally {
            log.info("[{}] {}: {}", guildName, authorName, query);
            event.getMessage().delete().queue();
        }
    }

}
