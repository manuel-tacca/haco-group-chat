package project;

import project.CLI.CLI;
import project.CLI.InputValidation;
import project.CLI.MenuKeyword;
import project.Communication.NetworkUtils;
import project.Exceptions.EmptyRoomException;
import project.Exceptions.InvalidRoomNameException;
import project.Exceptions.PeerAlreadyPresentException;
import project.Exceptions.SameRoomNameException;
import project.Model.CreatedRoom;
import project.Model.Room;

import java.io.IOException;
import java.security.InvalidParameterException;
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
                        case MenuKeyword.QUIT:
                            inScanner.close();
                            break;
                        case MenuKeyword.CREATE:
                            if(!client.getPeers().isEmpty()) {
                                // 1. prendi in input un ip ed un port per il multicast, controlla se sono validi, scegli i peers e chiama il metodo createRoom sul client
                                /*do {
                                    CLI.printAskMulticastAddress();
                                    inputLine = inScanner.nextLine();
                                }while(!client.checkCorrectIpFormat(inputLine));
                                String ip = inputLine;
                                
                                do {
                                    CLI.printAskMulticastPort();
                                    inputLine = inScanner.nextLine();
                                }while(!client.checkCorrectPortFormat(inputLine));
                                int port = Integer.parseInt(inputLine);*/
                                
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
                        case MenuKeyword.CHAT:
                            if (client.getCreatedRooms().isEmpty() && client.getParticipatingRooms().isEmpty()) {
                                CLI.printWarning("Bruv, there are no rooms yet! You may want to create one first!");
                            }
                            else {
                                client.chatInRoom(commands[1]);
                            }
                            break;
                        case MenuKeyword.LIST:
                            if (commands[1].equals(MenuKeyword.PEERS)) {
                                CLI.printPeers(client.getPeers());
                            } else {
                                CLI.printRooms(client.getCreatedRooms(), client.getParticipatingRooms());
                            }
                            break;
                        case MenuKeyword.DISCOVER:
                            client.discoverNewPeers();
                            break;
                        case MenuKeyword.DELETE:
                            client.deleteCreatedRoom(commands[1]);
                            CLI.printSuccess("The selected room has been deleted.");
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
