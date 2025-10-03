package chat.tortuga.discord.core.command;

import chat.tortuga.discord.core.exception.BotException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Getter
@AllArgsConstructor
public abstract class SlashCommand implements Consumer<SlashCommandInteractionEvent> {

    protected final String name;
    protected final String description;
    protected final Set<Permission> permissions;
    protected final Set<InteractionContextType> contexts;
    protected final boolean nsfw;

    protected SlashCommand(String name, String description) {
        this.name = name.trim().toLowerCase();
        this.description = description.trim();
        this.permissions = Set.of(Permission.MESSAGE_SEND);
        this.contexts = Set.of(InteractionContextType.GUILD);
        this.nsfw = false;
    }

    @Override
    public void accept(SlashCommandInteractionEvent event) {
        try {
            handle(event);
        } catch (BotException e) {
            handle(event, e);
        } catch (Exception e) {
            handle(event, e);
        }
    }

    protected abstract void handle(SlashCommandInteractionEvent event) throws BotException;

    protected void handle(SlashCommandInteractionEvent event, BotException e) {
        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(Color.YELLOW)
                        .setTitle(e.getMessage())
                        .build())
                .setEphemeral(true)
                .queue();
    }

    private void handle(SlashCommandInteractionEvent event, Exception e) {
        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Error")
                        .setDescription(e.getMessage())
                        .build())
                .setEphemeral(true)
                .queue();
    }

    public SlashCommandData build() {
        return Commands.slash(name, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions))
                .setContexts(contexts)
                .setNSFW(nsfw);
    }
}
