package project.CLI;

import java.util.regex.Pattern;

/**
 * The InputValidation class provides utility methods for validating user input.
 * It includes methods to validate command line arguments and to check if strings
 * contain only alphanumeric characters.
 */
public class InputValidation {

    /**
     * Validates command line input based on predefined keywords and expected argument counts.
     *
     * @param commands The array of command line arguments.
     * @return true if the command is valid, false otherwise.
     */
    public static boolean validateCommandLine(String[] commands){
        if (commands.length == 0) return false;
        return switch (commands[0]) {
            case MenuKeyword.QUIT, MenuKeyword.UPDATE, MenuKeyword.DISCOVER, MenuKeyword.HELP -> commands.length == 1;
            case MenuKeyword.LIST ->
                    commands.length == 2 && (commands[1].equals(MenuKeyword.PEERS) || commands[1].equals(MenuKeyword.ROOMS));
            case MenuKeyword.CREATE, MenuKeyword.CHAT, MenuKeyword.DELETE -> commands.length == 2;
            default -> false;
        };
    }

    /**
     * Checks if a string contains only alphanumeric characters.
     *
     * @param str The string to be checked.
     * @return true if the string is alphanumeric, false otherwise.
     */
    public static boolean isStringAlphanumeric(String str) {
        // Uses a regular expression to check if the string contains only letters and numbers
        return Pattern.matches("[a-zA-Z0-9]+", str);
    }

}
