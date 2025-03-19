package chat.tortuga.discord.persistence.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = GuildSettings.COLLECTION, schemaVersion = "2.0")
public class GuildSettings {

    public static final String COLLECTION = "GUILD_SETTINGS";

    @Id
    private Long guildId;
    private Long musicChannelId;

    public GuildSettings(Long guildId) {
        this.guildId = guildId;
    }
}
