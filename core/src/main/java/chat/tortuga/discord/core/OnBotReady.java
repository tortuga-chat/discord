package chat.tortuga.discord.core;

import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.util.function.Consumer;

public interface OnBotReady extends Consumer<ReadyEvent> {

}
