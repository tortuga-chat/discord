package chat.tortuga.discord.command;

import chat.tortuga.discord.JellyfinService;
import chat.tortuga.discord.command.handler.SlashCommandHandler;
import chat.tortuga.discord.exception.BotException;

@Command(name = "refresh", description = "Refreshes all jellyfin libraries")
public class Refresh extends SlashCommandHandler {

    @Override
    protected void handle() throws BotException {
        try {
            JellyfinService.doRefresh();
        } catch (Exception e) {
            throw new BotException(e.getMessage());
        }
        interaction.reply("Refresh queued!")
                .setEphemeral(true)
                .queue();
    }

}
