package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.exception.GuildRequiredException;
import chat.tortuga.discord.music.service.MusicService;
import jakarta.enterprise.context.Dependent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;

@Dependent
public class Next extends SlashCommand {

    private static final String OPTION_QUERY = "query";
    private final MusicService service;

    protected Next(MusicService service) {
        super("next", "Plays track as the next song");
        this.service = service;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) throws BotException {
        if (event.getGuild() == null) throw new GuildRequiredException();

        String query = Optional.ofNullable(event.getOption(OPTION_QUERY))
                .map(OptionMapping::getAsString)
                .orElseThrow(() -> new BotException("Missing option " + OPTION_QUERY));

        service.handleUserQuery(event.getGuild(), event.getChannelIdLong(), event.getMember(), query, true);
    }

    @Override
    public SlashCommandData build() {
        return super.build()
                .addOption(OptionType.STRING, OPTION_QUERY, "A search query or a track link", true);
    }

}
