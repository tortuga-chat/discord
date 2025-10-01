package chat.tortuga.discord.music.command;

import chat.tortuga.discord.music.config.Music;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.Dependent;

@Dependent
public class YTMusic extends AbstractSearchCommand {

    private static final String SEARCH_PREFIX = "ytmsearch:";
    private final Music music;

    public YTMusic(MusicService service, Music music) {
        super(service, SEARCH_PREFIX, "ytmusic", "Searches for a track in youtube music");
        this.music = music;
    }

    @Override
    protected boolean isSourceDisabled() {
        return !music.sources().youtube() || !music.youtube().clients().music();
    }

}
