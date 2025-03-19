package chat.tortuga.discord;

import chat.tortuga.discord.persistence.Torrent;
import chat.tortuga.discord.persistence.TorrentService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class jellyfinListener extends EventListener {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (TorrentService.getTorrentByMessage(event.getMessageIdLong()) != null) return;
        log.info("Received reaction - Refreshing jellyfin and deleting the message.");
        try {
            event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
            Torrent torrent = TorrentService.getTorrentByMessage(event.getMessageIdLong());
            if (torrent != null) {
                TorrentService.deleteTorrent(torrent.getTorrentId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
