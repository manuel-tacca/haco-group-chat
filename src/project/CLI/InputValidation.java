package project.CLI;

public class InputValidation {

    public static boolean validate(String[] commands){
        if (commands.length == 0) return false;
        return switch (commands[0]) {
            case MenuKeyword.QUIT, MenuKeyword.UPDATE, MenuKeyword.DISCOVER -> commands.length == 1;
            case MenuKeyword.LIST ->
                    commands.length == 2 && (commands[1].equals(MenuKeyword.PEERS) || commands[1].equals(MenuKeyword.ROOMS));
            case MenuKeyword.CREATE, MenuKeyword.JOIN, MenuKeyword.DELETE -> commands.length == 2;
            default -> false;
        };
    }

}
