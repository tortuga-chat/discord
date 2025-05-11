package chat.tortuga.discord.task;

import chat.tortuga.discord.service.music.MusicService;

import java.time.Duration;
import java.time.Instant;

@Task(delay = "5", period = "5", unit = "MINUTES")
public class DisconnectOnIdleTask implements Runnable {

    @Override
    public void run() {
        MusicService.getManagers().forEach(manager -> {
            if (manager.getScheduler().hasCurrentTrack()) return;
            if (!manager.getScheduler().isEmpty()) return;
            if (manager.getPlayer().isPaused()) return;
            if (manager.getScheduler().getEndedAt() != null &&
                    Instant.now().isBefore(manager.getScheduler().getEndedAt().plus(Duration.ofMinutes(5)))) return;
            MusicService.disconnectFromVoiceChannel(manager.getGuild());
        });
    }

}
