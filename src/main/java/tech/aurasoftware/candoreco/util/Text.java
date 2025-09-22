package tech.aurasoftware.candoreco.util;

import org.bukkit.ChatColor;

/**
 * Utility class for text formatting and color code translation.
 * Provides methods for translating color codes and working with special characters.
 */
public class Text {
    
    public static char DOUBLE_ARROW_RIGHT = '»';
    public static char DOUBLE_ARROW_LEFT = '«';
    public static String STAR = "☆";
    public static String CHECKMARK = "✓";
    public static String X = "✗";
    public static String LUNAR = "✪";
    public static String LINE = "┃";

    /**
     * Converts time in milliseconds to a human-readable format
     * @param millis the time in milliseconds
     * @return formatted time string
     */
    public static String convertMillis(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(append("day", days));
        }
        if (hours % 24 > 0) {
            result.append(append("hour", hours % 24));
        }
        if (minutes % 60 > 0) {
            result.append(append("minute", minutes % 60));
        }
        if (seconds % 60 > 0) {
            result.append(append("second", seconds % 60));
        }

        String output = result.toString().trim();
        return output.isEmpty() ? "0 seconds" : output;
    }

    /**
     * Helper method for time formatting
     * @param type the time unit type
     * @param value the value
     * @return formatted string
     */
    private static String append(String type, long value) {
        if (value <= 0) return "";
        return value + " " + type + (value > 1 ? "s" : "") + " ";
    }

    /**
     * Translates color codes in a message using the ampersand (&) character.
     * Converts &a, &b, &c, etc. to their corresponding ChatColor values.
     * 
     * @param message the message with color codes
     * @return the message with translated colors
     */
    public static String c(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Removes all color codes from a message
     * @param message the message to strip colors from
     * @return the message without color codes
     */
    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(c(message));
    }

    /**
     * Centers text within a specified width
     * @param text the text to center
     * @param width the total width
     * @return centered text
     */
    public static String center(String text, int width) {
        if (text == null || text.length() >= width) {
            return text;
        }
        
        int spaces = (width - stripColor(text).length()) / 2;
        StringBuilder centered = new StringBuilder();
        
        for (int i = 0; i < spaces; i++) {
            centered.append(" ");
        }
        centered.append(text);
        
        return centered.toString();
    }

    /**
     * Repeats a character a specified number of times
     * @param character the character to repeat
     * @param times the number of repetitions
     * @return the repeated character string
     */
    public static String repeat(char character, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(character);
        }
        return result.toString();
    }

    /**
     * Repeats a string a specified number of times
     * @param text the string to repeat
     * @param times the number of repetitions
     * @return the repeated string
     */
    public static String repeat(String text, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(text);
        }
        return result.toString();
    }
}