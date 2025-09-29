package chat.tortuga.discord.music.config;

import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import io.smallrye.config.ConfigMapping;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "music")
public interface Music {

    Sources sources();
    Youtube youtube();
    DisconnectOnIdle disconnectOnIdle();

    interface Sources {
        boolean youtube();
        boolean twitch();
        boolean soundcloud();
        boolean bandcamp();
        boolean nicoaudio();
        boolean vimeo();
        boolean yandex();
        boolean beampro();
        boolean getyarn();
        boolean http();
        boolean local();
    }

    interface Youtube {

        Oauth oauth();
        Optional<Cipher> cipher();
        Clients clients();
        boolean loadMix();

        interface Oauth {
            boolean enabled();
            Optional<String> token();
        }

        interface Cipher {
            String url();
            Optional<String> password();
        }

        interface Clients {
            boolean tv();
            boolean music();
            boolean web();

            default Client[] instances() {
                List<Client> clients = new ArrayList<>();

                if(tv()) clients.add(new TvHtml5EmbeddedWithThumbnail());
                if(music()) clients.add(new MusicWithThumbnail());
                if(web()) clients.addAll(List.of(new WebWithThumbnail(), new MWebWithThumbnail(), new WebEmbeddedWithThumbnail()));

                return clients.toArray(new Client[0]);
            }
        }
    }

    interface DisconnectOnIdle {
        Duration idleFor();
        String checkEvery();
    }

}
