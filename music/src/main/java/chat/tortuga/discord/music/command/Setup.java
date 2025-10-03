package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.exception.GuildRequiredException;
import chat.tortuga.discord.music.persistence.model.GuildSettings;
import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

@Dependent
public class Setup extends SlashCommand {

    private static final String CHANNEL_OPTION = "channel";

    private final GuildSettingsRepository repository;

    public Setup(GuildSettingsRepository repository) {
        super("setup", "Sets up the server",
                Set.of(Permission.ADMINISTRATOR), Set.of(InteractionContextType.GUILD), false);

        this.repository = repository;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) throws BotException {
        if (event.getGuild() == null) throw new GuildRequiredException();

        final Guild guild = event.getGuild();
        final OptionMapping option = event.getOption(CHANNEL_OPTION);
        final GuildSettings settings = repository.find(guild.getIdLong()).orElse(new GuildSettings(guild.getIdLong()));

        if (option == null)
            guild.createTextChannel("tortuga-music").queue(t -> save(settings, t.getIdLong()));
        else
            save(settings, option.getAsChannel().asGuildMessageChannel().getIdLong());

        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Done!")
                        .setDescription(String.format("%s was set as the dedicated channel",
                                Objects.requireNonNull(guild.getTextChannelById(settings.getMusicChannelId())).getAsMention()))
                        .build())
                .setEphemeral(true)
                .queue();
    }

    private void save(GuildSettings settings, Long id) {
        settings.setMusicChannelId(id);
        repository.save(settings);
    }

    @Override
    public SlashCommandData build() {
        return super.build()
                .addOption(OptionType.CHANNEL, CHANNEL_OPTION, "Dedicated music channel", false);
    }
}
