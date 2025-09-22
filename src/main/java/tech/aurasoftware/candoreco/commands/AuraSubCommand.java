package tech.aurasoftware.candoreco.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods as subcommands of a main command.
 * Methods annotated with this must have a single parameter of type CommandContext.
 * The parent command must be defined by an @AuraCommand annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuraSubCommand {
    
    /**
     * The parent command name this subcommand belongs to
     * @return the parent command name
     */
    String parent();
    
    /**
     * The name of the subcommand
     * @return the subcommand name
     */
    String name();
    
    /**
     * Optional description of the subcommand
     * @return the subcommand description
     */
    String description() default "";
    
    /**
     * Optional permission required to execute the subcommand
     * @return the permission node
     */
    String permission() default "";
    
    /**
     * Optional usage message for the subcommand
     * @return the usage string
     */
    String usage() default "";
    
    /**
     * Optional aliases for the subcommand
     * @return array of alias names
     */
    String[] aliases() default {};
}