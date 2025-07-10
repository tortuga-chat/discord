package chat.tortuga.discord.http;

import chat.tortuga.discord.JellyfinBot;
import chat.tortuga.discord.JellyfinService;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.IOException;

@Slf4j
public class DownloadFinishedHandler extends LogHandler {

    private static final String HEADER_ID = "X-Torrent-Id";
    private static final String HEADER_NAME = "X-Torrent-Name";
    private static final String HEADER_PATH = "X-Torrent-Path";
    private static final String HEADER_TAGS = "X-Torrent-Tags";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
        final String id = exchange.getRequestHeaders().getFirst(HEADER_ID);
        final String name = exchange.getRequestHeaders().getFirst(HEADER_NAME);
        final String path = exchange.getRequestHeaders().getFirst(HEADER_PATH);
        final String tags = exchange.getRequestHeaders().getFirst(HEADER_TAGS);
        try {
            JellyfinBot.getInstance().sendUpdateMessage(downloadStatusEmbed(id, name, path, tags));
            JellyfinService.doRefresh();
            exchange.sendResponseHeaders(200, -1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exchange.sendResponseHeaders(500, -1);
        }
    }

    protected EmbedBuilder downloadStatusEmbed(String id, String name, String file, String tags) {
        log.debug("Creating embed with name {}, file {}, tags {}", name, file, tags);
        EmbedBuilder embed = new EmbedBuilder()
                .setFooter(id, "https://img.icons8.com/?size=100&id=5b4t8ZwtA5p7&format=png&color=000000")
                .setTitle("Download Finished - Refreshing Jellyfin")
                .addField("Torrent", name, true);

        if (file != null && !file.isEmpty())
            embed.addField("File", file, true);
        if (tags != null && !tags.isEmpty())
            embed.addField("Tags", tags, true);

        return embed.setColor(Color.orange);
    }

}
