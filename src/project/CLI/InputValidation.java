package project.CLI;

import java.util.regex.Pattern;

public class InputValidation {

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

    public static boolean isStringAlphanumeric(String str) {
        // Utilizza un'espressione regolare per controllare se la stringa contiene solo lettere e numeri
        return Pattern.matches("[a-zA-Z0-9]+", str);
    }

}
