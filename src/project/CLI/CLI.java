package project.CLI;

import project.Client;
import project.Model.Notification;
import project.Model.Peer;
import project.Model.Room;
import project.Model.RoomText;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The CLI class handles the command line interface interactions.
 * It includes methods for printing room and peers details, formatting notifications,
 * drawing containers for displaying content in a structured format and more.
 */
public class CLI {

    private static boolean debugMode = false;
    private static final PrintStream out = System.out;

    private static final String BOLD = "\033[1m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String BLUE = "\033[34m";
    private static final String VIOLET = "\033[35m";
    private static final String ORANGE = "\033[33m";
    private static final String RESET = "\033[0m";

    private static final int WIDTH = 100;
    private static final String PADDING = "  ";

    private static final String APP_HEADER = BOLD + "HACO Group Chat v1.0.0" + RESET;
    private static final String ASK_FOR_COMMAND = "haco-v1.0.0> ";
    private static final String ASK_FOR_INPUT = "> ";

    private static final List<Notification> notifications = new ArrayList<>();
    private static String output = null;

    /**
     * Enables debug output and disables console auto-clear.
     */
    public static void enterDebugMode(){
        debugMode = true;
    }

    /**
     * Prints the user's nickname and UUID. Will also print the notifications if present.
     *
     * @param user The client.
     * @param showHelp Flag, if true the method will also print the list of commands available.
     */
    public static void printMenu(Client user, boolean showHelp){
        drawContainer(APP_HEADER, false);
        drawContainer("Your nickname: " + BOLD + BLUE + user.getPeerData().getUsername() + RESET + "\n" +
                PADDING + "Your UUID: " + user.getPeerData().getIdentifier() , false);
        drawContainer(BOLD + "Notification center:\n" + RESET + formatNotifications(), !showHelp && output == null);
        if(output != null){
            drawContainer( BOLD + "Output:\n" + RESET + output, !showHelp);
            output = null;
        }
        if(showHelp){
            drawContainer("Available commands:\n" + PADDING +
                    BOLD + "create " + RESET + "[room_name] -> creates a room with the given name\n" + PADDING +
                    BOLD + "chat " + RESET + "[room_name] -> enters the room with the given name\n" + PADDING +
                    BOLD + "delete " + RESET + "[room_name] -> deletes the room with the given name\n" + PADDING +
                    BOLD + "list " + RESET + "[peers|rooms] -> prints the reachable peers or the available rooms, based on the given command\n" + PADDING +
                    BOLD + "update" + RESET + " -> refreshes the interface, potentially showing new notifications\n" + PADDING +
                    BOLD + "discover" + RESET + " -> pings all the nearby peers\n" + PADDING +
                    BOLD + "help" + RESET + " -> prints this explanation\n" + PADDING +
                    BOLD + "quit" + RESET + " -> safely quits the application", true);
        }
    }

    /**
     * Prints infos about the peers currently connected to the network that can be added to the new room.
     *
     * @param peers Peers that can be added to the new room.
     */
    public static void printCreateRoomMenu(Set<Peer> peers){
        out.println(BOLD + "These are the peers currently connected to the network:" + RESET);
        printPeers(peers);
        printQuestion("Enter the whitespace-separated list of peer numbers you want to invite:");
    }

    /**
     * Prints infos about the rooms that have the same name to help disambiguate them.
     *
     * @param sameNameRooms Rooms with the same name.
     */
    public static void printDisambiguateRoomMenu(List<Room> sameNameRooms){
        out.println(BOLD + "There is more than one room with the name provided." + RESET);
        CLI.printRoomsInfo(sameNameRooms);
        printQuestion("Enter the number to disambiguate:");
    }

    /**
     * Prints the nickname and UUID of each peer in the provided set.
     *
     * @param peers Set of peers.
     */
    public static void printPeers(Set<Peer> peers){
        int index = 1;
        for(Peer peer: peers){
            out.println(PADDING + index + ".\tNickname: " + peer.getUsername() + "\n\t\tUUID: " + peer.getIdentifier());
            index++;
        }
    }

    /**
     * Prints the name and UUID of each room in the provided list.
     *
     * @param rooms Set of rooms provided.
     */
    public static void printRoomsInfo(List<Room> rooms){
        int index = 1;
        for(Room room: rooms){
            out.println(PADDING + index + ".\tRoom name: " + room.getName() + "\n\t\tRoom UUID: " + room.getIdentifier());
            index++;
        }
    }

    /**
     * Prints the name and UUID of the provided room.
     *
     * @param room Provided room.
     */
    public static void printRoomInfo(Room room){
        printRoomInfo(room, true);
    }

    /**
     * Prints the name and UUID of the provided room.
     *
     * @param room Provided room.
     * @param isFinal Boolean, necessary for the drawContainer method.
     */
    public static void printRoomInfo(Room room, boolean isFinal){
        Set<String> users = new HashSet<>();
        room.getRoomMembers().forEach(x -> users.add(x.getUsername()));
        String usersString = "";
        for(String user: users) {
            usersString = usersString.concat(PADDING + "\t- ").concat(user).concat("\n");
        }
        usersString = usersString.substring(0, usersString.length() - 1);
        drawContainer("Room name: " + BOLD + BLUE + room.getName() + RESET + "\n" + PADDING
                    + "Room UUID: " + room.getIdentifier() + "\n" + PADDING
                    + "Room members:\n"
                    + usersString, isFinal, false);
    }

    /**
     * Prints the messages of a room.
     *
     * @param roomTexts List of messages to be printed.
     * @param myself The client.
     */
    public static void printRoomMessages(List<RoomText> roomTexts, Peer myself){
        for(RoomText roomText: roomTexts) {
            if (roomText.author().equals(myself)) {
                out.println("[" + roomText.author().getUsername() + "]: " + BOLD + roomText.content() + RESET);
            } else {
                out.println("[" + roomText.author().getUsername() + "]: " + roomText.content());
            }
        }
    }

    /**
     * Prints the string provided with the style associated to questions.
     *
     * @param string String to be printed.
     */
    public static void printQuestion(String string){
        out.println(BOLD + string + RESET);
        out.print(ASK_FOR_INPUT);
    }

    /**
     * Prints the string provided with the style associated to errors.
     *
     * @param string String to be printed.
     */
    public static void printError(String string){
        out.println(BOLD + RED + string + RESET);
    }

    /**
     * Prints the string provided with the style associated to debugging.
     *
     * @param string String to be printed.
     */
    public static void printDebug(String string){
        if(debugMode) {
            out.println(BOLD + ORANGE + string + RESET);
        }
    }

    /**
     * Prints an empty string to standard output.
     */
    public static void printToExit(){
        out.print("");
    }

    /**
     * Prints in the output the nickname and UUID of each peer in the provided set.
     *
     * @param peers Set of peers.
     */
    public static void putPeersListInOutput(Set<Peer> peers){
        output = "";
        int index = 1;
        for(Peer peer: peers){
            output = output.concat( PADDING + index + ".\tNickname: " + peer.getUsername() + "\n\t\tUUID: " + peer.getIdentifier() + "\n");
            index++;
        }
        output = output.substring(0, output.length() - 1); // removes useless new line
    }

    /**
     * Prints the name and UUID of each room from the sets of created and participating rooms.
     *
     * @param createdRooms Set of rooms created.
     * @param participatingRooms Set of rooms participated in.
     */
    public static void putRoomsListInOutput(Set<Room> createdRooms, Set<Room> participatingRooms){
        output = PADDING + "[bold rooms were created by you]\n";
        int index = 1;
        for(Room room: createdRooms){
            output = output.concat(PADDING + BOLD + index + ".\tName: " + room.getName() + "\n\t\tUUID: " + room.getIdentifier() + RESET + "\n");
            index++;
        }
        for(Room room: participatingRooms){
            output = output.concat(PADDING + index + ".\tName: " + room.getName() + "\n\t\tUUID: " + room.getIdentifier() + "\n");
            index++;
        }
        output = output.substring(0, output.length() - 1); // removes useless new line
    }

    /**
     * Formats the notifications in a specific style based on their type.
     * Each notification is formatted with a different color and bold text depending on its type
     * (SUCCESS, WARNING, ERROR, INFO). The formatted notifications are concatenated into a single
     * string, separated by padding.
     *
     * @return A formatted string of all notifications or "None." if there are no notifications.
     */
    private static String formatNotifications(){
        if(!notifications.isEmpty()) {
            String result = PADDING;
            for (Notification notification : notifications) {
                switch(notification.type()){
                    case SUCCESS -> result = result.concat(BOLD + GREEN + notification.content() + RESET);
                    case WARNING -> result = result.concat(BOLD + YELLOW + notification.content() + RESET);
                    case ERROR -> result = result.concat(BOLD + RED + notification.content() + RESET);
                    case INFO -> result = result.concat(BOLD + VIOLET + notification.content() + RESET);
                }
                result = result.concat("\n" + PADDING);
            }
            result = result.substring(0, result.length() - 3);
            notifications.clear();
            return result;
        }
        else{
            return PADDING + "None.";
        }
    }

    /**
     * Method used to clear the console from previous prints.
     */
    public static void clearConsole() {
        if(!debugMode) {
            // ANSI escape code to clear console for both Windows and Unix-like systems
            out.print("\033[H\033[2J");
            out.flush();
        }
    }

    /**
     * Appends a notification to the notifications list.
     *
     * @param notification The notification to append.
     */
    public static void appendNotification(Notification notification){
        notifications.add(notification);
    }

    /**
     * Draws a container around the provided content.
     *
     * @param content The content to be displayed inside the container.
     * @param isFinal If true, draws a bottom border for the container.
     */
    private static void drawContainer(String content, boolean isFinal){
        if(isFinal) {
            drawContainer(content, true, true);
        }
        else{
            drawContainer(content, false, false);
        }
    }

    /**
     * Draws a container around the provided content, with options for a bottom border and a command prompt.
     *
     * @param content The content to be displayed inside the container.
     * @param isFinal If true, draws a bottom border for the container.
     * @param askForCommand If true, prompts the user for a command.
     */
    private static void drawContainer(String content, boolean isFinal, boolean askForCommand) {

        // Draw the top border
        out.print("+");
        for (int i = 0; i < WIDTH - 2; i++) {
            out.print("-");
        }
        out.println("+");

        out.println(PADDING + content);

        if(isFinal) {
            // Draw the bottom border
            out.print("+");
            for (int i = 0; i < WIDTH - 2; i++) {
                out.print("-");
            }
            out.println("+");
        }

        if(askForCommand){
            out.print(ASK_FOR_COMMAND);
        }
    }

}
