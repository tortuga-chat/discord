package chat.tortuga.discord.command;

import chat.tortuga.discord.command.handler.TortugaCommandHandler;
import chat.tortuga.discord.persistence.model.GuildSettings;
import chat.tortuga.discord.service.GuildSettingsService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Objects;

@Slf4j
@Command(name = "setup", description = "Setup the bot", permissions = Permission.ADMINISTRATOR, contexts = InteractionContextType.GUILD)
public class Setup extends TortugaCommandHandler {

    private GuildSettings settings;

    @Override
    protected void handle() {
        interaction.deferReply(true).queue();

        settings = GuildSettingsService.findById(guild.getIdLong())
                .orElse(new GuildSettings(guild.getIdLong()));

        if (!Objects.isNull(settings.getMusicChannelId())) {
            event.getHook()
                    .editOriginal(MessageEditData.fromEmbeds(new EmbedBuilder().setTitle("Already setup!").build()))
                    .queue();
            return;
        }

        guild.createTextChannel("tortuga-music")
                .queue(this::success, this::failed);
    }

    protected void success(TextChannel channel) {
        log.info("Created voice channel");
        settings.setMusicChannelId(channel.getIdLong());
        GuildSettingsService.save(settings);

        event.getHook()
                .editOriginal(MessageEditData.fromEmbeds(new EmbedBuilder().setTitle("Setup completed!").build()))
                .queue();
    }

    protected void failed(Throwable err) {
        log.error("Failed to create voice channel: {}", err.getMessage());

        event.getHook()
                .editOriginal(MessageEditData.fromEmbeds(new EmbedBuilder().setTitle(err.getMessage()).build()))
                .queue();
    }

}
