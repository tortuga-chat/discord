package chat.tortuga.discord.service;

import chat.tortuga.discord.persistence.DAO;
import chat.tortuga.discord.persistence.model.GuildSettings;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class GuildSettingsService {

    private static final DAO<GuildSettings, Long> dao = new DAO<>(GuildSettings.class);

    private GuildSettingsService() {}

    public static boolean exists(Long id) {
        return dao.exists(id);
    }

    public static List<GuildSettings> findAll() {
        return dao.findAll();
    }

    public static Optional<GuildSettings> findById(Long id) {
        return dao.findById(id);
    }

    public static void save(GuildSettings guildSettings) {
        if(guildSettings.getGuildId() == null)
            throw new IllegalArgumentException("guildId is not set!");

        if(exists(guildSettings.getGuildId()))
            dao.save(guildSettings);
        else
            dao.insert(guildSettings);

        log.info("Saved guild preferences {}", guildSettings);
    }

    public static boolean isChannelNotManaged(Long channelId) {
        return dao.findAll().stream()
                .map(GuildSettings::getMusicChannelId)
                .noneMatch(s -> s.equals(channelId));
    }

}
