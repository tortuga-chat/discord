package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.exception.GuildRequiredException;
import chat.tortuga.discord.music.exception.SetupRequiredException;
import chat.tortuga.discord.music.persistence.model.GuildSettings;
import chat.tortuga.discord.music.persistence.repository.GuildSettingsRepository;
import chat.tortuga.discord.music.service.playlist.message.GuildPlayerMessageService;
import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

import static chat.tortuga.discord.music.util.TrackUtils.*;

@Dependent
public class Clear extends SlashCommand {

    private final GuildPlayerMessageService messageService;
    private final GuildSettingsRepository repository;

    protected Clear(GuildPlayerMessageService messageService, GuildSettingsRepository repository) {
        super("clear", "Deletes all messages in the dedicated channel");
        this.messageService = messageService;
        this.repository = repository;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) throws BotException {
        final GuildSettings settings = Optional.ofNullable(event.getGuild())
                .map(ISnowflake::getIdLong)
                .map(repository::find)
                .orElseThrow(GuildRequiredException::new) // if guild is null
                .orElseThrow(SetupRequiredException::new);// if settings is null

        final TextChannel configured = Optional.ofNullable(settings.getMusicChannelId())
                .map(id -> event.getGuild().getTextChannelById(id))
                .orElseThrow(SetupRequiredException::new);

        if (configured.getIdLong() != event.getChannelIdLong())
            throw new BotException(String.format("This command only works in %s", configured.getAsMention()));

        messageService.cleanUp(configured);

        event.replyEmbeds(
                new EmbedBuilder()
                        .setTitle(String.format("%s Done!", EMOJI_STARS))
                        .build())
                .setEphemeral(true)
                .queue();
    }

}
