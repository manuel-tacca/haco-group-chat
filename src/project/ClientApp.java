package project;

import project.CLI.CLI;
import project.CLI.InputValidation;
import project.CLI.MenuKeyword;
import project.Exceptions.EmptyRoomException;
import project.Exceptions.InvalidRoomNameException;
import project.Exceptions.PeerAlreadyPresentException;
import project.Exceptions.SameRoomNameException;
import project.Model.Peer;
import project.Model.Room;
import project.Model.RoomMessage;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) throws IOException {

        Scanner inScanner = new Scanner(System.in);

        CLI.printJoin();

        Client client;
        try {
            String nickname = inScanner.nextLine();
            client = new Client(nickname);
        }
        catch(Exception e){
            throw new RuntimeException();
        }

        Thread listenerThread = new Thread(client.getListener());
        listenerThread.start();

        try {
            client.discoverNewPeers();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        String inputLine = MenuKeyword.QUIT;
        do{
            try {

                CLI.printMenu(client);
                inputLine = inScanner.nextLine();
                inputLine = inputLine.trim().toLowerCase();
                String[] commands = inputLine.split(" ");

                if (InputValidation.validate(commands)) {

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
                                CLI.printSuccess("Room " + commands[1] + " was created.");
                            }
                            else{
                                CLI.printWarning("There are no peers connected to the network yet.");
                            }
                            break;

                        // joins a room
                        case MenuKeyword.CHAT:
                            String roomName = commands[1];
                            if (client.getCreatedRooms().isEmpty() && client.getParticipatingRooms().isEmpty()) {
                                CLI.printWarning("You have not joined a chat room yet.");
                            }
                            else {
                                if (client.existsRoom(roomName)){
                                    client.setCurrentlyDisplayedRoom(client.getRoom(roomName));
                                    CLI.printRoomInfo(client.getCurrentlyDisplayedRoom());
                                    CLI.printRoomMessages(client.getRoomMessages(roomName));
                                    String message = null;
                                    do{
                                        if(message != null) {
                                            RoomMessage roomMessage = new RoomMessage(client.getPeerData(), message, true);
                                            client.sendRoomMessage(roomMessage);
                                            CLI.printNewMessage(roomMessage);
                                        }
                                        message = inScanner.nextLine();
                                    }
                                    while(!message.equalsIgnoreCase("exit"));
                                }
                                else{
                                    CLI.printError("No chat with such a name exists: " + commands[1]);
                                }
                            }
                            break;

                        // lists the discovered peers or the joined rooms
                        case MenuKeyword.LIST:
                            if (commands[1].equals(MenuKeyword.PEERS)) {
                                CLI.printPeers(client.getPeers());
                            } else {
                                CLI.printRooms(client.getCreatedRooms(), client.getParticipatingRooms());
                            }
                            break;

                        // pings all the peers in the same LAN
                        case MenuKeyword.DISCOVER:
                            client.discoverNewPeers();
                            break;

                        // deletes a room
                        case MenuKeyword.DELETE:
                            client.deleteCreatedRoom(commands[1]);
                            CLI.printSuccess("The selected room has been deleted.");

                        // refreshes the menu with the up-to-date information
                        case MenuKeyword.UPDATE:
                        default:
                            break;
                    }
                } else {
                    CLI.printError("No such command: " + inputLine);
                }
            }
            catch (IndexOutOfBoundsException e1) {
                CLI.printError("There's no peer with such a number.");
            } catch (PeerAlreadyPresentException e2) {
                CLI.printError("Such peer is already present in the room.");
            } catch (EmptyRoomException e3) {
                CLI.printError("You tried to create an empty room. Please try again");
            } catch (InvalidRoomNameException e4) {
                CLI.printError("There is no room that can be deleted with the name provided.");
            } catch (SameRoomNameException e5) {
                CLI.printError("There is more than one room that can be deleted with the name provided.");
                CLI.printRoomsInfo(e5.getFilteredRooms());
                // checks: the input has to be an integer and has to be within the size of the filtered rooms
                while (!inScanner.hasNextInt() || inScanner.nextInt() > e5.getFilteredRooms().size() ||
                        inScanner.nextInt() <= 0 ) {
                    CLI.printError("The input provided is not valid, please try again.");
                }
                Room selectedRoom = e5.getFilteredRooms().get(inScanner.nextInt()-1);
                client.deleteCreatedRoomMultipleChoice(selectedRoom);
                CLI.printSuccess("The room selected has been deleted.");
            }

            catch (Exception e) {
                e.printStackTrace();
                CLI.printError("The given input is incorrect. Please try again.");
            }
        } while (!inputLine.equals(MenuKeyword.QUIT));

        client.close();
    }
}
