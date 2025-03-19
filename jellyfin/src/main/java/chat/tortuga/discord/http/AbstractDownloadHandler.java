package chat.tortuga.discord.http;

import chat.tortuga.discord.JellyfinBot;
import chat.tortuga.discord.JellyfinService;
import chat.tortuga.discord.persistence.TorrentService;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.io.IOException;

@Slf4j
public abstract class AbstractDownloadHandler extends LogHandler {

    private static final String HEADER_ID = "X-Torrent-Id";
    private static final String HEADER_NAME = "X-Torrent-Name";
    private static final String HEADER_PATH = "X-Torrent-Path";
    private static final String HEADER_TAGS = "X-Torrent-Tags";

    private final boolean shouldDoRefresh;

    public AbstractDownloadHandler(boolean shouldDoRefresh) {
        this.shouldDoRefresh = shouldDoRefresh;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
        final String id = exchange.getRequestHeaders().getFirst(HEADER_ID);
        final String name = exchange.getRequestHeaders().getFirst(HEADER_NAME);
        final String path = exchange.getRequestHeaders().getFirst(HEADER_PATH);
        final String tags = exchange.getRequestHeaders().getFirst(HEADER_TAGS);
        try {
            Message message = JellyfinBot.getInstance().sendUpdateMessage(downloadStatusEmbed(name, path, tags));
            if (shouldDoRefresh)
                JellyfinService.doRefresh();

            persist(id, message);
            exchange.sendResponseHeaders(200, -1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exchange.sendResponseHeaders(500, -1);
        }
    }

    protected void persist(String torrentId, Message message) {
        TorrentService.registerTorrent(torrentId, message.getIdLong());
    }

    protected EmbedBuilder downloadStatusEmbed(String name, String file, String tags) {
        log.debug("Creating embed with name {}, file {}, tags {}", name, file, tags);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(getEmbedTitle())
                .addField("Torrent", name, true);

        if (file != null && !file.isEmpty())
            embed.addField("File", file, true);
        if (tags != null && !tags.isEmpty())
            embed.addField("Tags", tags, true);

        return embed.setColor(Color.orange);
    }

    protected abstract String getEmbedTitle();
}
