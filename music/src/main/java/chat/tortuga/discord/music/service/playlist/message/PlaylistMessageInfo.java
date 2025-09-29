package chat.tortuga.discord.music.service.playlist.message;

import java.util.List;

public record PlaylistMessageInfo(PlayerMessageInfo previous, PlayerMessageInfo current,
                                  List<PlayerMessageInfo> playlist, boolean loopingPlaylist) {
}
