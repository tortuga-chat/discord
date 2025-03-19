package chat.tortuga.discord.persistence;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Torrent.COLLECTION, schemaVersion = "1.0")
public class Torrent implements Serializable {

    public static final String COLLECTION = "torrents";
    @Id
    private String torrentId;
    private Long messageId;
    private boolean finished;
    private Instant lastUpdate;

}
