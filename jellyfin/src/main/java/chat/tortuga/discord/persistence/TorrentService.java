package chat.tortuga.discord.persistence;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
public class TorrentService {

    private static final DAO<Torrent, String> dao = new DAO<>(Torrent.class);

    private TorrentService() {}

    public static Torrent getTorrent(String id) {
        log.info("Retrieving torrent with id {}", id);
        return dao.findById(id).orElse(null);
    }

    public static Torrent getTorrentByMessage(long id) {
        log.info("Retrieving torrent with messageId {}", id);
        List<Torrent> list = dao.find(String.format("/.[messageId='%d']", id));
        return list.isEmpty() ? null : list.getFirst();
    }

    public static void registerTorrent(String id, long messageId) {
        log.info("Registering torrent with id {}", id);
        if (dao.exists(id))
            dao.save(new Torrent(id, messageId, false, Instant.now()));
        else
            dao.insert(new Torrent(id, messageId, false, Instant.now()));
    }

    public static void updateTorrent(String id, long messageId, boolean finished) {
        log.info("Updating torrent with id {}", id);
        dao.findById(id).ifPresent(torrent -> {
            torrent.setMessageId(messageId);
            torrent.setFinished(finished);
            torrent.setLastUpdate(Instant.now());
            dao.save(torrent);
        });
    }

    public static void deleteTorrent(String id) {
        log.info("Deleting torrent with id {}", id);
        dao.findById(id).ifPresent(dao::remove);
    }

}
