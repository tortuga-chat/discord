package chat.tortuga.discord.config;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Music {

    public static final String FILE = "music.yml";
    
    private Youtube youtube;

    @Getter
    public static class Youtube {

        Oauth oauth;

        @Getter
        public static class Oauth {

            String token;
            boolean enabled = false;
            boolean init = false;
        }
    }

}
