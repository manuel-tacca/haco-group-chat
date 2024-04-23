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

public class CLI {

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

    public static void printMenu(Client user, boolean showHelp){
        //clearConsole();
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

    public static void printCreateRoomMenu(Set<Peer> peers){
        out.println(BOLD + "These are the peers currently connected to the network:" + RESET);
        printPeers(peers);
        printQuestion("Enter the whitespace-separated list of peer numbers you want to invite:");
    }

    public static void printDisambiguateRoomMenu(List<Room> sameNameRooms){
        out.println(BOLD + "There is more than one room with the name provided." + RESET);
        CLI.printRoomsInfo(sameNameRooms);
        printQuestion("Enter the number to disambiguate:");
    }

    public static void printPeers(Set<Peer> peers){
        int index = 1;
        for(Peer peer: peers){
            out.println(PADDING + index + ".\tNickname: " + peer.getUsername() + "\n\t\tUUID: " + peer.getIdentifier());
            index++;
        }
    }

    public static void printRoomsInfo(List<Room> rooms){
        int index = 1;
        for(Room room: rooms){
            out.print(PADDING + index + ".\tNickname: " + room.getName() + "\n\t\tUUID: " + room.getIdentifier());
            index++;
        }
    }

    public static void printRoomInfo(Room room){
        printRoomInfo(room, true);
    }

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

    public static void printRoomMessages(List<RoomText> roomTexts, Peer myself){
        for(RoomText roomText: roomTexts) {
            if (roomText.author().equals(myself)) {
                out.println("[" + roomText.author().getUsername() + "]: " + BOLD + roomText.content() + RESET);
            } else {
                out.println("[" + roomText.author().getUsername() + "]: " + roomText.content());
            }
        }
    }

    public static void printQuestion(String string){
        out.println(BOLD + string + RESET);
        out.print(ASK_FOR_INPUT);
    }

    public static void printError(String string){
        out.println(BOLD + RED + string + RESET);
    }

    public static void printDebug(String string){
        out.println(BOLD + ORANGE + string + RESET);
    }

    public static void putPeersListInOutput(Set<Peer> peers){
        output = "";
        int index = 1;
        for(Peer peer: peers){
            output = output.concat( PADDING + index + ".\tNickname: " + peer.getUsername() + "\n\t\tUUID: " + peer.getIdentifier() + "\n");
            index++;
        }
        output = output.substring(0, output.length() - 1); // removes useless new line
    }

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

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (final Exception e) {
            out.println("Could not clean the console: " + e.getMessage());
        }
    }

    public static void appendNotification(Notification notification){
        notifications.add(notification);
    }

    private static void drawContainer(String content, boolean isFinal){
        if(isFinal) {
            drawContainer(content, true, true);
        }
        else{
            drawContainer(content, false, false);
        }
    }

    private static void drawContainer(String content, boolean isFinal, boolean askForCommand) {

        // Disegna il bordo superiore
        out.print("+");
        for (int i = 0; i < WIDTH - 2; i++) {
            out.print("-");
        }
        out.println("+");

        out.println(PADDING + content);

        if(isFinal) {
            // Disegna il bordo inferiore
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
