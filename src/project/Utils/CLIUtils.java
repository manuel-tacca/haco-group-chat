package project.Utils;

import java.io.PrintStream;
import java.util.List;
import project.Peer;

public class CLIUtils {

    private static final PrintStream out = System.out;

    public static void printPeers(List<Peer> peers){
        int i=1;
        for (Peer peer : peers) {
            out.println();
            out.println("project.Peer"+i+":");
            out.println("\tID: " + peer.getIdentifier());
            out.println("\tUsername: " + peer.getUsername());
            out.println();
            i++;
        }
    }

}
