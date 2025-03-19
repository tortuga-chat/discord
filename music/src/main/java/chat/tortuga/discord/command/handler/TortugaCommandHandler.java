package chat.tortuga.discord.command.handler;

import chat.tortuga.discord.exception.BotException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;

@Slf4j
public abstract class TortugaCommandHandler extends GuildSlashCommandHandler {

    @Override
    protected void handleException(BotException e) {
        super.handleException(e);
        replyException(warnEmbed(e));
    }

    @Override
    protected void handleException(Exception e) {
        if (log.isDebugEnabled())
            super.handleException(e);
        else
            log.error(e.getMessage());
        replyException(errorEmbed(e));
    }

    protected void replyException(MessageEmbed embed) {
        if (event.isAcknowledged())
            event.getHook()
                    .editOriginal(MessageEditData.fromEmbeds(embed))
                    .queue();
        else
            event.getInteraction()
                    .reply(MessageCreateData.fromEmbeds(embed))
                    .setEphemeral(true)
                    .queue();
    }

    protected MessageEmbed warnEmbed(BotException e) {
        return new EmbedBuilder()
                .setTitle(e.getMessage())
                .setColor(Color.yellow)
                .build();
    }

    protected MessageEmbed errorEmbed(Exception e) {
        return new EmbedBuilder()
                .setTitle("Error!")
                .setDescription(e.getMessage())
                .setColor(Color.red)
                .build();
    }

}
