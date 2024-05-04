package project;

import project.CLI.CLI;
import project.CLI.InputValidation;
import project.CLI.MenuKeyword;
import project.Exceptions.*;
import project.Model.*;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {

        Scanner inScanner = new Scanner(System.in);

        Client client = null;
        String nickname;
        boolean isNicknameValid;
        boolean isClientCreated = false;
        do {
            CLI.printQuestion("Please, enter an alphanumeric nickname:");
            nickname = inScanner.nextLine().trim();
            isNicknameValid = InputValidation.isStringAlphanumeric(nickname);
            if(!isNicknameValid){
                CLI.printError("The given nickname is not alphanumeric.");
            }
            else{
                try {
                    client = new Client(nickname);
                    isClientCreated = true;
                }
                catch(Exception e){
                    CLI.printError("Something went wrong. Are you connected to the network?");
                }
            }
        }while(!isNicknameValid || !isClientCreated);

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
                                CLI.printCreateRoomMenu(client.getPeers());
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
                                        chat(client, roomName, inScanner);
                                    } else {
                                        CLI.appendNotification(new Notification(NotificationType.ERROR, "No chat with such a name exists: " + commands[1]));
                                    }
                                } catch (InvalidParameterException e1){
                                    CLI.appendNotification(new Notification(NotificationType.WARNING, "You can no longer chat in the room '" + commands[1] + "' because it was deleted by its creator."));
                                }
                                catch(SameRoomNameException e2){
                                    Room selectedRoom = disambiguateRoom(e2.getFilteredRooms(), inScanner);
                                    chat(client, selectedRoom.getName(), inScanner);
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
                                CLI.appendNotification(new Notification(NotificationType.ERROR, e1.getMessage()));
                            }
                            catch (SameRoomNameException e2) {
                                Room selectedRoom = disambiguateRoom(e2.getFilteredRooms(), inScanner);
                                client.deleteCreatedRoom(selectedRoom);
                                CLI.appendNotification(new Notification(NotificationType.SUCCESS, "The selected room has been deleted."));
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
            catch (InputMismatchException e1){
                CLI.appendNotification(new Notification(NotificationType.ERROR, "The provided input is not valid, please try again."));
            }
            catch (Exception e) {
                CLI.appendNotification(new Notification(NotificationType.ERROR, "Oops, something went wrong. Please try again."));
            }
        } while (!inputLine.equals(MenuKeyword.QUIT));

        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Room disambiguateRoom(List<Room> filteredRooms, Scanner inScanner){
        CLI.printDisambiguateRoomMenu(filteredRooms);
        boolean done = false;
        int choice = 0;
        do {
            try {
                choice = inScanner.nextInt();
                if(choice <= 0 || choice > filteredRooms.size()){
                    CLI.printQuestion("The input must be between 1 and " + filteredRooms.size() + ". Please try again.");
                }
                else {
                    done = true;
                }
            } catch (InputMismatchException e1) {
                CLI.printQuestion("The input is not valid. Please try again.");
            }
        }
        while(!done);
        inScanner.nextLine(); // for some reason this is necessary
        return filteredRooms.get(choice-1);
    }

    private static void chat(Client client, String roomName, Scanner inScanner) throws InvalidParameterException {
        client.setCurrentlyDisplayedRoom(client.getRoom(roomName));
        String message = null;
        do {
            if (client.getCurrentlyDisplayedRoom() == null) {
                break;
            }
            if (message != null && !message.equalsIgnoreCase("update")) {
                RoomText roomText = new RoomText(client.getCurrentlyDisplayedRoom().getIdentifier(),
                        client.getPeerData(), message);
                try{
                    client.sendRoomText(roomText);
                }
                catch (IOException ignored){
                    // this way, users can still write messages even if disconnected from the network
                }
            }
            
            Room currentlyDisplayedRoom = client.getCurrentlyDisplayedRoom();
            CLI.printRoomInfo(currentlyDisplayedRoom);
            CLI.printRoomMessages(currentlyDisplayedRoom.getRoomMessages(), client.getPeerData());
            CLI.printQuestion("Type your message here: [type 'update' to receive messages (if any), 'exit' to go back to the menu]");
            message = inScanner.nextLine();

        } while (!message.equalsIgnoreCase("exit") && !(client.getCurrentlyDisplayedRoom() == null));
    }

}
