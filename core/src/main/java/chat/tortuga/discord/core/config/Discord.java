package chat.tortuga.discord.core.config;

import io.smallrye.config.ConfigMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "discord", namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE)
public interface Discord {

    String token();
    Optional<Set<GatewayIntent>> intents();
    Optional<Boolean> updateGlobalCommands();

}
