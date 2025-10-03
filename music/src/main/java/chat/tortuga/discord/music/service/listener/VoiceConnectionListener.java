package chat.tortuga.discord.music.service.listener;

import chat.tortuga.discord.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class VoiceConnectionListener implements ConnectionListener {

    private final MusicService service;
    private final Guild guild;

    @Override
    public void onStatusChange(@NotNull ConnectionStatus status) {
        if (!status.shouldReconnect()) {
            log.debug("[{}] Disconnected", guild.getName());
            service.handleDisconnect(guild);
        }
    }

}
