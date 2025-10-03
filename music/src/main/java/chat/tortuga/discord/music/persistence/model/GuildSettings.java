package chat.tortuga.discord.music.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static chat.tortuga.discord.music.persistence.model.GuildSettings.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@NamedQuery(name = QUERY_FIND_ALL, query = "SELECT g FROM GuildSettings g")
@NamedQuery(name = QUERY_FIND_BY_CHANNEL, query = "SELECT g FROM GuildSettings g WHERE g.musicChannelId = :" + QUERY_PARAMETER_CHANNEL)
public class GuildSettings {

    public static final String QUERY_FIND_ALL = "findAll";
    public static final String QUERY_FIND_BY_CHANNEL = "findByChannel";
    public static final String QUERY_PARAMETER_CHANNEL = "channelId";

    @Id
    private Long guildId;
    private Long musicChannelId;

    public GuildSettings(Long guildId) {
        this.guildId = guildId;
    }
}
