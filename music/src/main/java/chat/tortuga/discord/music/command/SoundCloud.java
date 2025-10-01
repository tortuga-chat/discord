package chat.tortuga.discord.music.command;

import chat.tortuga.discord.music.config.Music;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.Dependent;

@Dependent
public class SoundCloud extends AbstractSearchCommand {

    public static final String SEARCH_PREFIX = "scsearch:";
    private final Music music;

    public SoundCloud(MusicService service, Music music) {
        super(service, SEARCH_PREFIX, "soundcloud", "Searches for a track in soundcloud");
        this.music = music;
    }

    @Override
    protected boolean isSourceDisabled() {
        return !music.sources().soundcloud();
    }
}
