package tech.aurasoftware.candoreco.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods as Aura commands.
 * Methods annotated with this must have a single parameter of type CommandContext.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuraCommand {
    
    /**
     * The name of the command
     * @return the command name
     */
    String name();
    
    /**
     * Optional description of the command
     * @return the command description
     */
    String description() default "";
    
    /**
     * Optional permission required to execute the command
     * @return the permission node
     */
    String permission() default "";
    
    /**
     * Optional usage message for the command
     * @return the usage string
     */
    String usage() default "";
}