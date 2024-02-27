import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        Scanner inScanner = new Scanner(System.in);

        System.out.println("Welcome to the groupchat!");
        System.out.println("What is your username?");
        client.setUsername(inScanner.nextLine());
        System.out.println("Very good, "+client.getUsername());

        Thread listenerThread = new Thread(client.getListener());
        listenerThread.start();
        client.sendPing();
        
        /*System.out.println("What do you want to do?");
        System.out.println("1. Create a room");
        System.out.println("2. Print the discovered peers");
        System.out.println("0. Exit");
        int menuChoice = inScanner.nextInt();

        if (menuChoice == 2){
            client.printPeers();
        }*/
    }
}
