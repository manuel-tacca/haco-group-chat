package project;

import project.CLI.CLI;
import project.CLI.InputValidation;
import project.CLI.MenuKeyword;
import project.Exceptions.*;
import project.Model.*;

import java.io.IOException;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {

        Scanner inScanner = new Scanner(System.in);

        Client client;
        String nickname;
        boolean isNicknameValid;
        try {
            do {
                CLI.printQuestion("Please, enter an alphanumeric nickname:");
                nickname = inScanner.nextLine().trim();
                isNicknameValid = InputValidation.isStringAlphanumeric(nickname);
                if(!isNicknameValid){
                    CLI.printError("The given nickname is not alphanumeric.");
                }
            }while(!isNicknameValid);
            client = new Client(nickname);
        }
        catch(Exception e){
            throw new RuntimeException();
        }

        try {
            client.discoverNewPeers();
        }
        catch(IOException e){
            CLI.appendNotification(new Notification(NotificationType.ERROR,
                    "There was an error trying to contact the other peers. Please use the 'discover' command to try again."));
        }
        
        String inputLine = MenuKeyword.QUIT;
        boolean showHelp = false;
        do{
            try {

                CLI.printMenu(client, showHelp);
                showHelp = false;
                inputLine = inScanner.nextLine();
                inputLine = inputLine.trim().toLowerCase();
                String[] commands = inputLine.split(" ");

                if (InputValidation.validateCommandLine(commands)) {

                    switch (commands[0]) {

                        // closes the application
                        case MenuKeyword.QUIT:
                            inScanner.close();
                            break;

                        // creates a room
                        case MenuKeyword.CREATE:
                            if(!client.getPeers().isEmpty()) {
                                CLI.printPeers(client.getPeers());
                                CLI.printQuestion("Enter the ids of the peers you want to invite:");
                                inputLine = inScanner.nextLine();
                                String[] peerIds = inputLine.trim().split(" ");
                                client.createRoom(commands[1], peerIds);
                                CLI.appendNotification(new Notification(NotificationType.SUCCESS, "Room " + commands[1] + " was created."));
                            }
                            else{
                                CLI.appendNotification(new Notification(NotificationType.WARNING, "There are no peers connected to the network yet."));
                            }
                            break;

                        // joins a room
                        case MenuKeyword.CHAT:
                            String roomName = commands[1];
                            if (client.getCreatedRooms().isEmpty() && client.getParticipatingRooms().isEmpty()) {
                                CLI.appendNotification(new Notification(NotificationType.WARNING, "You have joined no chat room yet."));
                            }
                            else {
                                try {
                                    if (client.existsRoom(roomName)) {
                                        client.setCurrentlyDisplayedRoom(client.getRoom(roomName));
                                        String message = null;
                                        do {
                                            if (message != null && !message.equalsIgnoreCase("update")) {
                                                RoomText roomText = new RoomText(client.getCurrentlyDisplayedRoom().getIdentifier(),
                                                        client.getPeerData(), message);
                                                client.sendRoomText(roomText);
                                                
                                            }
                                            CLI.printRoomInfo(client.getCurrentlyDisplayedRoom());
                                            CLI.printRoomMessages(client.getRoomMessages(roomName), client.getPeerData());
                                            CLI.printQuestion("Type your message here: [type 'update' to receive messages (if any), 'exit' to go back to the menu]");
                                            message = inScanner.nextLine();
                                        }
                                        while (!message.equalsIgnoreCase("exit"));
                                    } else {
                                        CLI.appendNotification(new Notification(NotificationType.ERROR, "No chat with such a name exists: " + commands[1]));
                                    }
                                } catch (InvalidParameterException e1){
                                    CLI.appendNotification(new Notification(NotificationType.WARNING, "You can no longer chat in the room '" + commands[1] + "' because it was deleted by its creator."));
                                }
                                catch(SameRoomNameException e2){
                                    //TODO: disambiguare quando due chat hanno lo stesso nome
                                }
                            }
                            break;

                        // lists the discovered peers or the joined rooms
                        case MenuKeyword.LIST:
                            if (commands[1].equals(MenuKeyword.PEERS)) {
                                if(!client.getPeers().isEmpty()) {
                                    CLI.putPeersListInOutput(client.getPeers());
                                }
                                else{
                                    CLI.appendNotification(new Notification(NotificationType.WARNING, "No peer has joined the network yet."));
                                }
                            } else {
                                if(!client.getCreatedRooms().isEmpty() || !client.getParticipatingRooms().isEmpty()) {
                                    CLI.putRoomsListInOutput(client.getCreatedRooms(), client.getParticipatingRooms());
                                }
                                else{
                                    CLI.appendNotification(new Notification(NotificationType.WARNING, "You have joined no room yet. You can create one now with the 'create [room_name]' command."));
                                }
                            }
                            break;

                        // pings all the peers in the same LAN
                        case MenuKeyword.DISCOVER:
                            client.discoverNewPeers();
                            break;

                        // deletes a room
                        case MenuKeyword.DELETE:
                            try {
                                client.deleteCreatedRoom(commands[1]);
                                CLI.appendNotification(new Notification(NotificationType.SUCCESS, "The following room has been deleted: " + commands[1]));
                            }
                            catch (InvalidParameterException e1){
                                CLI.appendNotification(new Notification(NotificationType.ERROR, "There is no room with such a name: " + commands[1]));
                            }
                            catch (SameRoomNameException e2) {
                                CLI.printQuestion("There is more than one room that can be deleted with the name provided.");
                                CLI.printRoomsInfo(e2.getFilteredRooms());
                                // checks: the input has to be an integer and has to be within the size of the filtered rooms
                                while (!inScanner.hasNextInt() || inScanner.nextInt() > e2.getFilteredRooms().size() ||
                                        inScanner.nextInt() <= 0 ) {
                                    CLI.appendNotification(new Notification(NotificationType.ERROR, "The input provided is not valid, please try again."));
                                }
                                Room selectedRoom = e2.getFilteredRooms().get(inScanner.nextInt()-1);
                                client.deleteCreatedRoomMultipleChoice(selectedRoom);
                                CLI.appendNotification(new Notification(NotificationType.SUCCESS, "The room selected has been deleted."));
                            }
                            break;

                        // displays the menu once again with the list of available commands
                        case MenuKeyword.HELP:
                            showHelp = true;
                            break;

                        // refreshes the menu with the up-to-date information
                        case MenuKeyword.UPDATE:
                        default:
                            break;
                    }
                } else {
                    CLI.appendNotification(new Notification(NotificationType.ERROR, "No such command: " + inputLine));
                }
            }

            catch (Exception e) {
                e.printStackTrace(); //TODO: before meeting with Cugola, it should be removed
                CLI.appendNotification(new Notification(NotificationType.ERROR, "Oops, something went wrong. Please try again."));
            }
        } while (!inputLine.equals(MenuKeyword.QUIT));

        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
