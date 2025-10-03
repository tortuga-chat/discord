package chat.tortuga.discord.jellyfin.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jellyfin")
public interface Jellyfin {

    Long channelId();
    String footerIconUrl();
    String emoji();

}
