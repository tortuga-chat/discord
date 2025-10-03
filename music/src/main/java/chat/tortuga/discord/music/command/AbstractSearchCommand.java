package chat.tortuga.discord.music.command;

import chat.tortuga.discord.core.command.SlashCommand;
import chat.tortuga.discord.core.exception.BotException;
import chat.tortuga.discord.music.exception.GuildRequiredException;
import chat.tortuga.discord.music.service.MusicService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;

public abstract class AbstractSearchCommand extends SlashCommand {

    private static final String OPTION_QUERY = "query";
    private final MusicService service;
    private final String searchPrefix;

    protected AbstractSearchCommand(MusicService service, String searchPrefix, String name, String description) {
        super(name, description);
        this.searchPrefix = searchPrefix;
        this.service = service;
    }

    @Override
    protected void handle(SlashCommandInteractionEvent event) throws BotException {
        if (event.getGuild() == null) throw new GuildRequiredException();
        if (isSourceDisabled()) throw new BotException(String.format("%s is disabled!", name));

        String query = Optional.ofNullable(event.getOption(OPTION_QUERY))
                .map(OptionMapping::getAsString)
                .map(searchPrefix::concat)
                .orElseThrow(() -> new BotException("Missing option " + OPTION_QUERY));

        service.handleUserQuery(event.getGuild(), event.getChannelIdLong(), event.getMember(), query);
        event.reply("Loaded!").setEphemeral(true).queue();
    }

    @Override
    public SlashCommandData build() {
        return super.build()
                .addOption(OptionType.STRING, OPTION_QUERY, "The query to search", true);
    }

    protected abstract boolean isSourceDisabled();

}
