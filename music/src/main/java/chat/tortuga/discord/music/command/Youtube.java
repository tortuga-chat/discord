package chat.tortuga.discord.music.command;

import chat.tortuga.discord.music.config.Music;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.Dependent;

@Dependent
public class Youtube extends AbstractSearchCommand {

    private static final String SEARCH_PREFIX = "ytsearch:";
    private final Music music;

    public Youtube(MusicService service, Music music) {
        super(service, SEARCH_PREFIX, "youtube", "Searches for a track in youtube");
        this.music = music;
    }

    @Override
    protected boolean isSourceDisabled() {
        return !music.sources().youtube();
    }
}
