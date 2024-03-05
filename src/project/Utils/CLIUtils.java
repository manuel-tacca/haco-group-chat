package project.Utils;

import java.io.PrintStream;
import java.util.List;
import project.Peer;
import project.Rooms.CreatedRoom;
import project.Rooms.Room;

public class CLIUtils {

    private static final PrintStream out = System.out;

    public static void printPeers(List<Peer> peers){
        int i=1;
        for (Peer peer : peers) {
            out.println();
            out.println("Peer"+i+":");
            out.println("\tID: " + peer.getIdentifier());
            out.println("\tUsername: " + peer.getUsername());
            out.println();
            i++;
        }
    }

    public static void printRooms(List<CreatedRoom> createdRooms, List<Room> participatingRooms){
        int index = 1;
        if (!createdRooms.isEmpty()) {
            out.println("Created Rooms: ");
            for (Room r : createdRooms){
                out.println("Room "+index+" : "+r.getName());
                out.println("ID: "+r.getIdentifier());
                index++;
            }
            out.println();
        }
        if (!participatingRooms.isEmpty()) {
            out.println("Participating Rooms: ");
            for (Room r : participatingRooms){
                out.println("Room "+index+" : "+r.getName());
                out.println("ID: "+r.getIdentifier());
                index++;
            }
            out.println();
        }
        if (participatingRooms.isEmpty() && createdRooms.isEmpty()) {
            out.println("There are no rooms yet!");
        }
    }

}
