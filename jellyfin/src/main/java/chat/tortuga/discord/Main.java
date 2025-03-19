package chat.tortuga.discord;

import chat.tortuga.discord.http.DownloadFinishedHandler;
import chat.tortuga.discord.http.DownloadStartedHandler;
import chat.tortuga.discord.http.RefreshHandler;
import chat.tortuga.discord.http.Server;
import chat.tortuga.discord.task.TaskLoader;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        if (Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("register"))) {
            JellyfinBot.getInstance().registerCommands();
            JellyfinBot.getInstance().shutdown();
            return;
        }
        JellyfinBot.getInstance();
        Server server = new Server(8088);
        server.createContext("/refresh/", new RefreshHandler());
        server.createContext("/downloadStarted/", new DownloadStartedHandler());
        server.createContext("/downloadFinished/", new DownloadFinishedHandler());
        server.start();

        TaskLoader.getInstance().start();
    }

}
