package chat.tortuga.discord.music.persistence.repository;

import chat.tortuga.discord.music.persistence.model.UserSettings;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class UserSettingsRepository {

    private final EntityManager entityManager;

    public Optional<UserSettings> find(@Nonnull Long id) {
        return Optional.ofNullable(entityManager.find(UserSettings.class, id));
    }

    public UserSettings findOrDefault(@Nonnull Long id) {
        return find(id).orElse(UserSettings.defaultSettings(id));
    }

    public void save(@Nonnull UserSettings settings) {
        log.debug("Saving UserSettings: {}", settings);
        entityManager.merge(settings);
    }

}
