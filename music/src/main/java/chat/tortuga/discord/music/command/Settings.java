package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.music.persistence.model.UserSettings;
import chat.tortuga.discord.music.persistence.repository.UserSettingsRepository;
import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.Optional;

@Dependent
public class Settings extends SlashCommand {

    protected static final String OPTION_PLAYLIST_FROM_TRACK = "load-playlist-from-track";

    private final UserSettingsRepository repository;

    protected Settings(UserSettingsRepository repository) {
        super("settings", "Your settings!");
        this.repository = repository;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) {
        UserSettings settings = repository.findOrDefault(event.getUser().getIdLong());

        Optional.ofNullable(event.getOption(OPTION_PLAYLIST_FROM_TRACK))
                .map(OptionMapping::getAsBoolean)
                .ifPresent(settings::setPlaylistFromTrack);

        repository.save(settings);

        event.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setTitle("Done!")
                                .build())
                .setEphemeral(true)
                .queue();
    }

    @Override
    public SlashCommandData build() {
        return super.build()
                .addOption(OptionType.BOOLEAN, OPTION_PLAYLIST_FROM_TRACK,
                        "Set to false if you don't want to load playlists attached to video's urls");
    }
}
