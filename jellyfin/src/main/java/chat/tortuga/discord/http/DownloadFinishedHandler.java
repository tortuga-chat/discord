package chat.tortuga.discord.http;

import chat.tortuga.discord.JellyfinBot;
import chat.tortuga.discord.persistence.TorrentService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

@Slf4j
public class DownloadFinishedHandler extends AbstractDownloadHandler {

    public DownloadFinishedHandler() {
        super(true);
    }

    @Override
    protected void persist(String torrentId, Message message) {
        long oldMessageId = TorrentService.getTorrent(torrentId).getMessageId();
        JellyfinBot.getInstance().getUpdatesChannel().deleteMessageById(oldMessageId).queue();
        TorrentService.updateTorrent(torrentId, message.getIdLong(), true);
    }

    @Override
    protected String getEmbedTitle() {
        return "Download Finished!";
    }

}
