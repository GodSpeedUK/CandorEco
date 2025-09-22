package tech.aurasoftware.candoreco.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling placeholder replacement in text.
 * Allows for dynamic text substitution using placeholder keys and values.
 */
public class Placeholder {
    
    private final String placeholder;
    private final String value;

    /**
     * Creates a new placeholder
     * @param placeholder the placeholder key (e.g., "{player}")
     * @param value the value to replace the placeholder with
     */
    public Placeholder(String placeholder, String value) {
        this.placeholder = placeholder;
        this.value = value;
    }

    /**
     * Gets the placeholder key
     * @return the placeholder key
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Gets the replacement value
     * @return the replacement value
     */
    public String getValue() {
        return value;
    }

    /**
     * Applies multiple placeholders to a text string
     * @param text the text containing placeholders
     * @param placeholders the placeholders to apply
     * @return the text with placeholders replaced
     */
    public static String apply(String text, Placeholder... placeholders) {
        if (text == null) {
            return null;
        }
        
        String result = text;
        for (Placeholder placeholder : placeholders) {
            if (placeholder != null && placeholder.getPlaceholder() != null && placeholder.getValue() != null) {
                result = result.replace(placeholder.getPlaceholder(), placeholder.getValue());
            }
        }
        return result;
    }

    /**
     * Applies placeholders from a list to a text string
     * @param text the text containing placeholders
     * @param placeholders the list of placeholders to apply
     * @return the text with placeholders replaced
     */
    public static String apply(String text, List<Placeholder> placeholders) {
        return apply(text, placeholders.toArray(new Placeholder[0]));
    }

    /**
     * Creates a placeholder for a player name
     * @param playerName the player's name
     * @return a placeholder for {player}
     */
    public static Placeholder player(String playerName) {
        return new Placeholder("{player}", playerName);
    }

    /**
     * Creates a placeholder for an amount
     * @param amount the amount value
     * @return a placeholder for {amount}
     */
    public static Placeholder amount(String amount) {
        return new Placeholder("{amount}", amount);
    }

    /**
     * Creates a placeholder for an amount (numeric)
     * @param amount the amount value
     * @return a placeholder for {amount}
     */
    public static Placeholder amount(double amount) {
        return new Placeholder("{amount}", String.valueOf(amount));
    }

    /**
     * Creates a placeholder for a balance
     * @param balance the balance value
     * @return a placeholder for {balance}
     */
    public static Placeholder balance(String balance) {
        return new Placeholder("{balance}", balance);
    }

    /**
     * Creates a placeholder for a balance (numeric)
     * @param balance the balance value
     * @return a placeholder for {balance}
     */
    public static Placeholder balance(double balance) {
        return new Placeholder("{balance}", String.valueOf(balance));
    }

    /**
     * Creates a placeholder for a target player
     * @param targetName the target player's name
     * @return a placeholder for {target}
     */
    public static Placeholder target(String targetName) {
        return new Placeholder("{target}", targetName);
    }

    /**
     * Creates a placeholder for an error message
     * @param error the error message
     * @return a placeholder for {error}
     */
    public static Placeholder error(String error) {
        return new Placeholder("{error}", error);
    }

    /**
     * Creates a placeholder for a currency name
     * @param currency the currency name
     * @return a placeholder for {currency}
     */
    public static Placeholder currency(String currency) {
        return new Placeholder("{currency}", currency);
    }

    /**
     * Creates multiple common placeholders at once
     * @param player the player name
     * @param amount the amount
     * @param balance the balance
     * @return array of placeholders
     */
    public static Placeholder[] economy(String player, String amount, String balance) {
        return new Placeholder[] {
            player(player),
            amount(amount),
            balance(balance)
        };
    }

    /**
     * Creates multiple common placeholders at once (numeric)
     * @param player the player name
     * @param amount the amount
     * @param balance the balance
     * @return array of placeholders
     */
    public static Placeholder[] economy(String player, double amount, double balance) {
        return new Placeholder[] {
            player(player),
            amount(amount),
            balance(balance)
        };
    }

    @Override
    public String toString() {
        return "Placeholder{" +
                "placeholder='" + placeholder + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Placeholder that = (Placeholder) obj;
        return placeholder.equals(that.placeholder);
    }

    @Override
    public int hashCode() {
        return placeholder.hashCode();
    }
}