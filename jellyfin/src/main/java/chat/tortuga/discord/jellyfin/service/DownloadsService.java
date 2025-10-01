package chat.tortuga.discord.jellyfin.service;

import chat.tortuga.discord.core.DiscordBot;
import chat.tortuga.discord.jellyfin.client.JellyfinClient;
import chat.tortuga.discord.jellyfin.config.Jellyfin;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Optional;

@Slf4j
@RequestScoped
@RequiredArgsConstructor
public class DownloadsService {

    @RestClient
    private JellyfinClient client;

    private final DiscordBot bot;
    private final Jellyfin jellyfin;

    public void downloadFinished(String id, String name, String path, String tags) {
        Optional.ofNullable(bot.getJda().getTextChannelById(jellyfin.channelId()))
                .ifPresent(c -> c.sendMessageEmbeds(downloadFinishedEmbed(id, name, path, tags).build())
                        .queue(success -> {
                            log.info("Sent download finished message (id:{}) for {}", success.getId(), name);
                            try(RestResponse<Void> response = client.libraryRefresh()) {
                                if (response.getStatus() >= 200 && response.getStatus() < 300)
                                    success.addReaction(Emoji.fromFormatted(jellyfin.emoji()))
                                            .queue();
                            }
                        }));
    }

    protected EmbedBuilder downloadFinishedEmbed(String id, String name, String path, String tags) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Download Finished! Refreshing Jellyfin libraries")
                .addField("Download", name, true)
                .setFooter(id, jellyfin.footerIconUrl());

        if (path != null && !path.isEmpty()) embed.addField("File", path, true);
        if (tags != null && !tags.isEmpty()) embed.addField("Tags", tags, true);

        return embed;
    }

}
