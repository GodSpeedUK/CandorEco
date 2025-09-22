package tech.aurasoftware.candoreco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for number formatting and parsing.
 * Handles large numbers with suffixes (K, M, B, T, etc.) and provides
 * methods for formatting currency amounts.
 */
public class NumberUtil {

    public static final String[] SUFFIXES = new String[]{
        "", "K", "M", "B", "T", "Q", "Qt", "S", "ST", "O", "N", "D", "UD", "DD", "Z"
    };

    private static final Pattern CHARACTERS_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[0-9]\\d{0,100}(\\.\\d{1,3})?");
    private static final BigDecimal BIG_DECIMAL_THOUSAND = BigDecimal.valueOf(1000);

    /**
     * Formats a BigDecimal number with appropriate suffix
     * @param number the number to format
     * @return formatted string with suffix
     */
    public static String formatBigDecimal(BigDecimal number) {
        if (number.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        if (number.compareTo(BIG_DECIMAL_THOUSAND) < 0) {
            return formatDecimal(number);
        }

        int suffixIndex = 0;
        BigDecimal workingNumber = number;

        while (workingNumber.compareTo(BIG_DECIMAL_THOUSAND) >= 0 && suffixIndex < SUFFIXES.length - 1) {
            workingNumber = workingNumber.divide(BIG_DECIMAL_THOUSAND, 3, RoundingMode.HALF_UP);
            suffixIndex++;
        }

        return formatDecimal(workingNumber) + SUFFIXES[suffixIndex];
    }

    /**
     * Formats a double number with appropriate suffix
     * @param number the number to format
     * @return formatted string with suffix
     */
    public static String formatNumber(double number) {
        return formatBigDecimal(BigDecimal.valueOf(number));
    }

    /**
     * Formats a decimal number to a clean string representation
     * @param number the number to format
     * @return formatted decimal string
     */
    private static String formatDecimal(BigDecimal number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }

    /**
     * Attempts to parse a formatted number string back to a BigDecimal
     * @param input the formatted string (e.g., "1.5K", "2.3M")
     * @return Optional containing the parsed number, or empty if parsing fails
     */
    public static Optional<BigDecimal> tryParse(String input) {
        try {
            return Optional.of(formattedToBigDecimal(input));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Converts a formatted number string to BigDecimal
     * @param input the formatted string
     * @return the BigDecimal value
     * @throws NumberFormatException if the input is invalid
     */
    public static BigDecimal formattedToBigDecimal(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Input cannot be null or empty");
        }

        input = input.trim().toUpperCase();

        // Try to parse as regular number first
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException ignored) {
            // Continue with suffix parsing
        }

        Matcher numberMatcher = NUMBERS_PATTERN.matcher(input);
        Matcher characterMatcher = CHARACTERS_PATTERN.matcher(input);

        if (!numberMatcher.find()) {
            throw new NumberFormatException("No valid number found in input: " + input);
        }

        String numberPart = numberMatcher.group();
        BigDecimal baseNumber;

        try {
            baseNumber = new BigDecimal(numberPart);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format: " + numberPart);
        }

        if (!characterMatcher.find()) {
            return baseNumber;
        }

        String suffix = characterMatcher.group();
        int multiplierIndex = getSuffixIndex(suffix);

        if (multiplierIndex == -1) {
            throw new NumberFormatException("Unknown suffix: " + suffix);
        }

        // Calculate the multiplier (1000^index)
        BigDecimal multiplier = BigDecimal.ONE;
        for (int i = 0; i < multiplierIndex; i++) {
            multiplier = multiplier.multiply(BIG_DECIMAL_THOUSAND);
        }

        return baseNumber.multiply(multiplier);
    }

    /**
     * Gets the index of a suffix in the SUFFIXES array
     * @param suffix the suffix to find
     * @return the index, or -1 if not found
     */
    private static int getSuffixIndex(String suffix) {
        for (int i = 0; i < SUFFIXES.length; i++) {
            if (SUFFIXES[i].equalsIgnoreCase(suffix)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Formats currency with commas for better readability
     * @param amount the amount to format
     * @return formatted currency string
     */
    public static String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount);
    }

    /**
     * Formats currency with commas for better readability
     * @param amount the amount to format
     * @return formatted currency string
     */
    public static String formatCurrency(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount);
    }

    /**
     * Checks if a string represents a valid number
     * @param input the string to check
     * @return true if it's a valid number, false otherwise
     */
    public static boolean isNumeric(String input) {
        return tryParse(input).isPresent();
    }

    /**
     * Safely converts a string to double, returning 0 if parsing fails
     * @param input the string to convert
     * @return the double value or 0 if parsing fails
     */
    public static double safeParseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Safely converts a string to int, returning 0 if parsing fails
     * @param input the string to convert
     * @return the int value or 0 if parsing fails
     */
    public static int safeParseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Rounds a double to the specified number of decimal places
     * @param value the value to round
     * @param places the number of decimal places
     * @return the rounded value
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}