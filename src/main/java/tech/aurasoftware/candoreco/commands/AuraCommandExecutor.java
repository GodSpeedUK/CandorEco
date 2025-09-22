package tech.aurasoftware.candoreco.commands;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Executor that handles routing commands to annotated methods
 */
public class AuraCommandExecutor implements CommandExecutor {
    
    private final Map<String, CommandMethod> commands = new HashMap<>();
    private final JavaPlugin plugin;
    
    public AuraCommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Register a command method with optional subcommands
     * @param commandName the name of the command
     * @param instance the instance containing the method
     * @param method the annotated method
     * @param annotation the AuraCommand annotation
     * @param subCommandMethods list of subcommand methods
     */
    public void registerCommand(String commandName, Object instance, Method method, AuraCommand annotation, List<Method> subCommandMethods) {
        // Create subcommand map
        Map<String, SubCommandMethod> subCommands = new HashMap<>();
        
        for (Method subMethod : subCommandMethods) {
            AuraSubCommand subAnnotation = subMethod.getAnnotation(AuraSubCommand.class);
            if (subAnnotation != null) {
                SubCommandMethod subCommandMethod = new SubCommandMethod(instance, subMethod, subAnnotation);
                
                // Register by name
                subCommands.put(subAnnotation.name().toLowerCase(), subCommandMethod);
                
                // Register by aliases
                for (String alias : subAnnotation.aliases()) {
                    subCommands.put(alias.toLowerCase(), subCommandMethod);
                }
            }
        }
        
        commands.put(commandName.toLowerCase(), new CommandMethod(instance, method, annotation, subCommands));
        
        // Register the command with Bukkit (existing code)
        PluginCommand pluginCommand = plugin.getCommand(commandName);
        if (pluginCommand == null) {
            // Try getting from server command map
            pluginCommand = plugin.getServer().getPluginCommand(commandName);
        }
        
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            if (!annotation.description().isEmpty()) {
                pluginCommand.setDescription(annotation.description());
            }
            if (!annotation.usage().isEmpty()) {
                pluginCommand.setUsage(annotation.usage());
            }
            if (!annotation.permission().isEmpty()) {
                pluginCommand.setPermission(annotation.permission());
            }
            
            plugin.getLogger().info("Registered command: " + commandName + " with " + subCommands.size() + " subcommands");
        } else {
            plugin.getLogger().warning("Failed to register command: " + commandName + " - command not found in plugin.yml");
        }
    }
    
    /**
     * Register a command method (backward compatibility)
     * @param commandName the name of the command
     * @param instance the instance containing the method
     * @param method the annotated method
     * @param annotation the AuraCommand annotation
     */
    public void registerCommand(String commandName, Object instance, Method method, AuraCommand annotation) {
        registerCommand(commandName, instance, method, annotation, List.of());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandMethod commandMethod = commands.get(command.getName().toLowerCase());
        
        if (commandMethod == null) {
            return false;
        }
        
        // Check if there are subcommands and an argument was provided
        if (args.length > 0 && !commandMethod.getSubCommands().isEmpty()) {
            String subCommandName = args[0].toLowerCase();
            SubCommandMethod subCommandMethod = commandMethod.getSubCommands().get(subCommandName);
            
            if (subCommandMethod != null) {
                // Check subcommand permissions
                AuraSubCommand subAnnotation = subCommandMethod.getAnnotation();
                if (!subAnnotation.permission().isEmpty() && !sender.hasPermission(subAnnotation.permission())) {
                    sender.sendMessage("§cYou don't have permission to use this subcommand.");
                    return true;
                }
                
                try {
                    // Create context with remaining arguments (excluding the subcommand name)
                    String[] subArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                    CommandContext context = new CommandContext(sender, label + " " + args[0], subArgs);
                    
                    subCommandMethod.getMethod().invoke(subCommandMethod.getInstance(), context);
                    return true;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error executing subcommand: " + command.getName() + " " + subCommandName, e);
                    sender.sendMessage("§cAn error occurred while executing the subcommand.");
                    return true;
                }
            }
        }
        
        // Execute main command if no subcommand was found or no args provided
        AuraCommand annotation = commandMethod.getAnnotation();
        if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        try {
            CommandContext context = new CommandContext(sender, label, args);
            commandMethod.getMethod().invoke(commandMethod.getInstance(), context);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error executing command: " + command.getName(), e);
            sender.sendMessage("§cAn error occurred while executing the command.");
            return true;
        }
    }
    
    /**
     * Inner class to hold command method information
     */
    private static class CommandMethod {
        private final Object instance;
        private final Method method;
        private final AuraCommand annotation;
        private final Map<String, SubCommandMethod> subCommands;
        
        public CommandMethod(Object instance, Method method, AuraCommand annotation, Map<String, SubCommandMethod> subCommands) {
            this.instance = instance;
            this.method = method;
            this.annotation = annotation;
            this.subCommands = subCommands;
        }
        
        public Object getInstance() {
            return instance;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public AuraCommand getAnnotation() {
            return annotation;
        }
        
        public Map<String, SubCommandMethod> getSubCommands() {
            return subCommands;
        }
    }
    
    /**
     * Inner class to hold subcommand method information
     */
    private static class SubCommandMethod {
        private final Object instance;
        private final Method method;
        private final AuraSubCommand annotation;
        
        public SubCommandMethod(Object instance, Method method, AuraSubCommand annotation) {
            this.instance = instance;
            this.method = method;
            this.annotation = annotation;
        }
        
        public Object getInstance() {
            return instance;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public AuraSubCommand getAnnotation() {
            return annotation;
        }
    }
}