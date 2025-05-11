package chat.tortuga.discord.config;

import lombok.Getter;

import java.util.List;

@Getter
@SuppressWarnings("unused")
public class CoreConfig {

    private String splashArt;
    private Discord discord;
    private Command command;
    private Task task;

    @Getter
    public static class Discord {
        private String token;
    }

    @Getter
    public static class Command {
        private String prefix;
        private List<String> disabled;
    }

    @Getter
    public static class Task {
        private List<String> disabled;
    }

}
