package tech.aurasoftware.candoreco.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Context wrapper for command execution, providing easy access to command sender,
 * arguments, and utility methods.
 */
public class CommandContext {
    
    private final CommandSender sender;
    private final String[] args;
    private final String label;
    
    public CommandContext(CommandSender sender, String label, String[] args) {
        this.sender = sender;
        this.args = args;
        this.label = label;
    }
    
    /**
     * Get the command sender
     * @return the CommandSender who executed the command
     */
    public CommandSender getSender() {
        return sender;
    }
    
    /**
     * Get the command sender as a Player (if applicable)
     * @return the Player who executed the command, or null if not a player
     */
    public Player getPlayer() {
        return sender instanceof Player ? (Player) sender : null;
    }
    
    /**
     * Check if the command sender is a player
     * @return true if sender is a player
     */
    public boolean isPlayer() {
        return sender instanceof Player;
    }
    
    /**
     * Get the command arguments
     * @return array of command arguments
     */
    public String[] getArgs() {
        return args;
    }
    
    /**
     * Get the command label used
     * @return the command label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Get the number of arguments
     * @return the argument count
     */
    public int getArgCount() {
        return args.length;
    }
    
    /**
     * Get an argument at a specific index
     * @param index the index of the argument
     * @return the argument at the index, or null if index is out of bounds
     */
    public String getArg(int index) {
        return index >= 0 && index < args.length ? args[index] : null;
    }
    
    /**
     * Get an argument at a specific index with a default value
     * @param index the index of the argument
     * @param defaultValue the default value if index is out of bounds
     * @return the argument or default value
     */
    public String getArg(int index, String defaultValue) {
        String arg = getArg(index);
        return arg != null ? arg : defaultValue;
    }
    
    /**
     * Join arguments starting from a specific index
     * @param startIndex the starting index
     * @return joined arguments with spaces
     */
    public String joinArgs(int startIndex) {
        if (startIndex >= args.length) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }
    
    /**
     * Send a message to the command sender
     * @param message the message to send
     */
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
    
    /**
     * Check if the sender has a specific permission
     * @param permission the permission to check
     * @return true if sender has permission
     */
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }
}