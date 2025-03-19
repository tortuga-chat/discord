package chat.tortuga.discord.http;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadStartedHandler extends AbstractDownloadHandler {

    public DownloadStartedHandler() {
        super(false);
    }

    @Override
    protected String getEmbedTitle() {
        return "Download started!";
    }

}
