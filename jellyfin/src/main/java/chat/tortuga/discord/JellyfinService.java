package chat.tortuga.discord;

import chat.tortuga.discord.config.ConfigLoader;
import chat.tortuga.discord.config.Jellyfin;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static chat.tortuga.discord.config.Jellyfin.AUTHORIZATION_HEADER;

@Slf4j
public class JellyfinService {

    private static final Jellyfin CONFIG = new ConfigLoader().load("jellyfin.yml", Jellyfin.class);

    public static void doRefresh() throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(CONFIG.getUrl()).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty(AUTHORIZATION_HEADER, CONFIG.getAuthToken());

        connection.setConnectTimeout(CONFIG.getTimeout());
        connection.setReadTimeout(CONFIG.getTimeout());
        connection.connect();

        int status = connection.getResponseCode();
        log.info("Request to {} returned with status {}", CONFIG.getUrl(), status);
    }

}
