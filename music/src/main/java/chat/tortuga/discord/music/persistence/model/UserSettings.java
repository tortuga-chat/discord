package chat.tortuga.discord.music.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    private Long userId;
    @Getter(AccessLevel.NONE)
    private boolean playlistFromTrack;

    public boolean shouldLoadPlaylistFromTrack() {
        return playlistFromTrack;
    }

    public static UserSettings defaultSettings(Long userId) {
        return new UserSettings(userId, false);
    }

}
