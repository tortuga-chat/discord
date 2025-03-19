package chat.tortuga.discord.http;

import chat.tortuga.discord.JellyfinService;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RefreshHandler extends LogHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
        try {
            JellyfinService.doRefresh();
        } catch (Exception e) {
            log.error("Error handling refresh", e);
            exchange.sendResponseHeaders(500, -1);
            return;
        }
        exchange.sendResponseHeaders(200, -1);
    }

}
