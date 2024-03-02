import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Room {
    private String identifier; // TODO: usa uid come identifier
    private String name;
    private InetAddress creatorIP;
    private List<Peer> roomMembers;

    public Room(String name, InetAddress creatorIP, Peer p){
        this.name = name;
        this.creatorIP = creatorIP;
        roomMembers.add(p);
        generateUniqueIdentifier();
    }

    public List<Peer> getRoomMembers() {
        return roomMembers;
    }

    public void addPeer(Peer p){
        boolean inList = false;
        for (Peer pInList : roomMembers) {
            if (pInList.getIpAddress().equals(p.getIpAddress())) {
                inList = true;
                System.out.println("I am sorry! The selected peer is already on the list!");
                break;
            }
        }
        if (!inList) {
            roomMembers.add(p);
        }
    }

    public void printPeers(){
        int i=1;
        for (Peer peer : roomMembers) {
            System.out.println("Peer"+i+":");
            System.out.println("    Username: " + peer.getUsername());
            System.out.println("    IP Address: " + peer.getIpAddress().getHostAddress());
            System.out.println("    Port: " + peer.getPort());
            System.out.println();
            i++;
        }
    }

    public void generateUniqueIdentifier() {
        try {
            String combinedString = creatorIP + name;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedString.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            identifier = hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String getIdentifier(){
        return identifier;
    }
}
