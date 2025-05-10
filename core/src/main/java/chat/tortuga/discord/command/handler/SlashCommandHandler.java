package chat.tortuga.discord.command.handler;

import chat.tortuga.discord.command.Command;
import chat.tortuga.discord.exception.BotException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public abstract class SlashCommandHandler implements Consumer<SlashCommandInteractionEvent> {

    protected SlashCommandInteractionEvent event;
    protected SlashCommandInteraction interaction;
    protected MessageChannelUnion channel;
    protected User user;

    @Override
    public void accept(SlashCommandInteractionEvent event) {
        this.event = event;
        this.interaction = event.getInteraction();
        this.user = event.getUser();
        this.channel = event.getChannel();

        log.info("Channel={} User={} Command={}", channel.getName(), user.getName(), event.getFullCommandName());
        try {
            validate();
            handle();
        } catch (BotException e) {
            handleException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected void validate() throws BotException {}

    protected abstract void handle() throws BotException;

    protected void handleException(BotException e) {
        log.warn("BotException: {}", e.getMessage());
    }

    protected void handleException(Exception e) {
        log.error(e.getMessage(), e);
    }

    protected OptionData[] getOptions() {
        return null;
    }

    protected SubcommandGroupData[] getSubcommandGroups() {
        return null;
    }

    protected SubcommandData[] getSubcommands() {
        return null;
    }

    public SlashCommandData build() {
        Command command = getClass().getAnnotation(Command.class);
        if (command == null) {
            log.error("Command annotation not found in {}", getClass().getName());
            return null;
        }
        SlashCommandData data = Commands.slash(command.name(), command.description())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permissions()))
                .setNSFW(command.nsfw())
                .setContexts(command.contexts());

        if (Objects.nonNull(getOptions()))
            data.addOptions(getOptions());
        if (Objects.nonNull(getSubcommandGroups()))
            data.addSubcommandGroups(getSubcommandGroups());
        if (Objects.nonNull(getSubcommands()))
            data.addSubcommands(getSubcommands());

        return data;
    }

}