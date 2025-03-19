package chat.tortuga.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();
    String description();
    Permission[] permissions() default {Permission.MESSAGE_SEND};
    InteractionContextType[] contexts() default {InteractionContextType.GUILD};
    boolean nsfw() default false;

}