package tech.aurasoftware.candoreco.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Registry for scanning and registering annotated command methods
 */
public class CommandRegistry {
    
    private final JavaPlugin plugin;
    private final AuraCommandExecutor executor;
    
    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = new AuraCommandExecutor(plugin);
    }
    
    /**
     * Register all annotated methods in the given object
     * @param commandHandler the object containing annotated command methods
     */
    public void registerCommands(Object commandHandler) {
        Class<?> clazz = commandHandler.getClass();
        
        // First pass: collect main commands and subcommands
        Map<String, Method> mainCommands = new HashMap<>();
        Map<String, List<Method>> subCommands = new HashMap<>();
        
        for (Method method : clazz.getDeclaredMethods()) {
            // Handle main commands
            if (method.isAnnotationPresent(AuraCommand.class)) {
                AuraCommand annotation = method.getAnnotation(AuraCommand.class);
                
                if (!isValidCommandMethod(method)) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Invalid command method signature: " + method.getName() + 
                        " in class " + clazz.getSimpleName() + 
                        ". Method must have exactly one parameter of type CommandContext.");
                    continue;
                }
                
                String commandName = annotation.name();
                if (commandName.isEmpty()) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Command name cannot be empty for method: " + method.getName() + 
                        " in class " + clazz.getSimpleName());
                    continue;
                }
                
                mainCommands.put(commandName.toLowerCase(), method);
            }
            
            // Handle subcommands
            if (method.isAnnotationPresent(AuraSubCommand.class)) {
                AuraSubCommand annotation = method.getAnnotation(AuraSubCommand.class);
                
                if (!isValidCommandMethod(method)) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Invalid subcommand method signature: " + method.getName() + 
                        " in class " + clazz.getSimpleName() + 
                        ". Method must have exactly one parameter of type CommandContext.");
                    continue;
                }
                
                String parentCommand = annotation.parent().toLowerCase();
                String subCommandName = annotation.name();
                
                if (parentCommand.isEmpty() || subCommandName.isEmpty()) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Parent command and subcommand name cannot be empty for method: " + method.getName() + 
                        " in class " + clazz.getSimpleName());
                    continue;
                }
                
                subCommands.computeIfAbsent(parentCommand, k -> new ArrayList<>()).add(method);
            }
        }
        
        // Register main commands with their subcommands
        for (Map.Entry<String, Method> entry : mainCommands.entrySet()) {
            String commandName = entry.getKey();
            Method mainMethod = entry.getValue();
            List<Method> subCommandMethods = subCommands.getOrDefault(commandName, new ArrayList<>());
            
            // Make methods accessible if needed
            if (!mainMethod.isAccessible()) {
                mainMethod.setAccessible(true);
            }
            
            for (Method subMethod : subCommandMethods) {
                if (!subMethod.isAccessible()) {
                    subMethod.setAccessible(true);
                }
            }
            
            AuraCommand mainAnnotation = mainMethod.getAnnotation(AuraCommand.class);
            executor.registerCommand(commandName, commandHandler, mainMethod, mainAnnotation, subCommandMethods);
        }
    }
    
    /**
     * Register commands from multiple command handler objects
     * @param commandHandlers the objects containing annotated command methods
     */
    public void registerCommands(Object... commandHandlers) {
        for (Object handler : commandHandlers) {
            registerCommands(handler);
        }
    }
    
    /**
     * Register commands from classes (will create instances)
     * @param commandHandlerClasses the classes containing annotated command methods
     */
    public void registerCommandClasses(Class<?>... commandHandlerClasses) {
        for (Class<?> clazz : commandHandlerClasses) {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                registerCommands(instance);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, 
                    "Failed to instantiate command handler class: " + clazz.getSimpleName(), e);
            }
        }
    }
    
    /**
     * Validate that a method has the correct signature for a command method
     * @param method the method to validate
     * @return true if the method signature is valid
     */
    private boolean isValidCommandMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        
        // Must have exactly one parameter of type CommandContext
        return parameterTypes.length == 1 && parameterTypes[0] == CommandContext.class;
    }
    
    /**
     * Get the command executor instance
     * @return the AuraCommandExecutor
     */
    public AuraCommandExecutor getExecutor() {
        return executor;
    }
}