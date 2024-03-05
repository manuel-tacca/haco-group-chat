package project;

import project.Utils.CLIUtils;

import java.io.PrintStream;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) throws Exception {

        final PrintStream out = System.out;

        Scanner inScanner = new Scanner(System.in);

        Boolean setup = false;
        out.println("Welcome to the groupchat!");
        out.println("What is your username?");
        Client client = new Client(inScanner.nextLine());
        out.println("Very good, "+client.getPeerData().getUsername());
        setup = true;
        if (setup){
            Thread listenerThread = new Thread(client.getListener());
            listenerThread.start();
            client.sendPing();
        }
        
        int menuChoice;
        do{
            out.println("What do you want to do?");
            out.println("1. Create a room");
            out.println("2. Print the discovered peers");
            out.println("3. Chat in a room");
            out.println("0. Exit");
            menuChoice = inScanner.nextInt();
            switch (menuChoice) {
                case 0:
                    inScanner.close();
                    break;
                case 1:
                    client.createRoomStart();
                    break;
                case 2:
                    CLIUtils.printPeers(client.getPeers());
                    break;
                case 3:
                    if (client.getCreatedRooms().isEmpty() && client.getParticipatingRooms().isEmpty()) {
                        out.println("Bruv, there are no rooms yet! You may want to create one first!");
                        break;
                    }
                    client.chatInRoom(client.chooseRoom());
                    break;
                default:
                    break;
            }
        }while (menuChoice != 0);

        client.close();
    }
}
