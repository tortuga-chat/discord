package chat.tortuga.discord.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LogHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Method: {}, URI: {}, Headers: {}", exchange.getRequestURI(), exchange.getRequestMethod(), exchange.getRequestHeaders());
    }

}
