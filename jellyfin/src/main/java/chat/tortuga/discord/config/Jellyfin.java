package chat.tortuga.discord.config;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Jellyfin {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private String url;
    private int timeout;
    private String authToken;

}
