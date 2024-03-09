package project;

import project.CLI.CLI;
import project.CLI.InputValidation;
import project.CLI.MenuKeyword;
import project.Exceptions.EmptyRoomException;
import project.Exceptions.PeerAlreadyPresentException;

import java.io.IOException;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args){

        Scanner inScanner = new Scanner(System.in);

        CLI.printJoin();

        Client client;
        try {
            client = new Client(inScanner.nextLine());
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
            } catch (Exception e) {
                CLI.printError("The given input is incorrect. Please try again.");
            }
        }while (!inputLine.equals(MenuKeyword.QUIT));

        client.close();
    }
}
