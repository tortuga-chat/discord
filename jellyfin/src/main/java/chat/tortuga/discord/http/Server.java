package chat.tortuga.discord.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Slf4j
public class Server {

    private final HttpServer server;

    public Server(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void createContext(String path, HttpHandler handler) {
        server.createContext(path, handler);
    }

    public void start() {
        server.start();
        log.info("HTTPServer started on {}", server.getAddress());
    }

}
