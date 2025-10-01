package chat.tortuga.discord.music.persistence.repository;

import chat.tortuga.discord.music.persistence.model.GuildSettings;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static chat.tortuga.discord.music.persistence.model.GuildSettings.*;

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class GuildSettingsRepository {

    private final EntityManager entityManager;

    public List<GuildSettings> findAll() {
        return entityManager.createNamedQuery(QUERY_FIND_ALL, GuildSettings.class).getResultList();
    }

    public Optional<GuildSettings> find(@Nonnull Long id) {
        return Optional.ofNullable(entityManager.find(GuildSettings.class, id));
    }

    public Optional<GuildSettings> findByChannelId(@Nonnull Long channelId) {
        return Optional.ofNullable(
                entityManager.createNamedQuery(QUERY_FIND_BY_CHANNEL, GuildSettings.class)
                        .setParameter(QUERY_PARAMETER_CHANNEL, channelId)
                        .getSingleResultOrNull()
        );
    }

    public void save(@Nonnull GuildSettings guildSettings) {
        log.debug("Saving GuildSettings: {}", guildSettings);
        entityManager.merge(guildSettings);
    }

    public boolean isNotDedicatedChannel(@Nonnull Long guildId, @Nonnull Long channelId) {
        return find(guildId)
                .map(GuildSettings::getMusicChannelId)
                .stream()
                .noneMatch(channelId::equals);
    }
}
