package project;

import project.CLI.CLI;
import project.Exceptions.EmptyRoomException;
import project.Exceptions.InvalidParameterException;
import project.Exceptions.PeerAlreadyPresentException;
import project.Model.Peer;
import project.Model.CreatedRoom;
import project.Model.Room;
import project.Communication.Listener;
import project.Communication.Messages.MessageBuilder;
import project.Communication.Messages.Message;
import project.Communication.Sender;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Client {

    private final Peer myself;
    private final InetAddress broadcastAddress;
    private final Listener listener;
    private final Sender sender;
    private final List<Peer> peers;
    private final List<CreatedRoom> createdRooms;
    private final List<Room> participatingRooms;
    private final Scanner inScanner;
    private int sequenceNumber;

    public Client(String username) throws Exception {
        peers = new ArrayList<>();
        createdRooms = new ArrayList<>();
        participatingRooms = new ArrayList<>();
        inScanner = new Scanner(System.in);
        sequenceNumber = 0;
        String ip;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), Sender.PORT_NUMBER);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        CLI.printDebug(ip);
        this.listener = new Listener(this);
        this.sender = new Sender();
        sender.sendPendingPacketsAtFixedRate(1);
        this.myself = new Peer(username, InetAddress.getByName(ip), Sender.PORT_NUMBER);
        this.broadcastAddress = extractBroadcastAddress(myself.getIpAddress());
    }

    public Listener getListener(){
        return listener;
    }

    public Peer getPeerData(){
        return myself;
    }

    public List<CreatedRoom> getCreatedRooms() {
        return createdRooms;
    }

    public List<Room> getParticipatingRooms() {
        return participatingRooms;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void addPeer(Peer p) throws PeerAlreadyPresentException{
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = MessageBuilder.ping(myself.getIdentifier().toString(), myself.getUsername(), broadcastAddress);
        sendPacket(pingMessage, null);
    }

    public void createRoom(String roomName, String[] peerIds) throws Exception {

        CreatedRoom room = new CreatedRoom(roomName);
        for(String peerId: peerIds){
            int id = Integer.parseInt(peerId);
            room.addPeer(peers.get(id - 1));
        }

        if (room.getOtherRoomMembers().isEmpty()) {
            throw new EmptyRoomException(null);
        }

        this.createdRooms.add(room);
        
        for (Peer p : room.getOtherRoomMembers()) {

            int sequenceNumber = getAndIncrementSequenceNumber();
            Message roomMemberStartMessage = MessageBuilder.roomMemberStart(getProcessID(), room.getIdentifier().toString(),
                    room.getName(), myself, room.getOtherRoomMembers().size(), p.getIpAddress(), sequenceNumber);
            sendPacket(roomMemberStartMessage, sequenceNumber);

            for (Peer p1 : room.getOtherRoomMembers()) {
                if (!p1.getIdentifier().toString().equals(p.getIdentifier().toString())) {
                    Message roomMemberMessage = MessageBuilder.roomMember(getProcessID(), room.getIdentifier().toString(), p1, p.getIpAddress(), getAndIncrementSequenceNumber());
                    sendPacket(roomMemberMessage, sequenceNumber);
                }
            }

        }
    }

    public void createRoomMembership(Peer creator, String roomID, String roomName, int membersNumber){
        Room room = new Room(roomID, roomName, membersNumber);
        CLI.appendNotification("You have been inserted in a new room by "+creator.getUsername()+"! The ID of the room is: "+roomID);
        try {
            room.addPeer(creator);
        }
        catch(PeerAlreadyPresentException e){
            throw new RuntimeException(e.getMessage());
        }
        participatingRooms.add(room);
    }

    public void addRoomMember(String roomID, Peer newPeer) throws Exception{
        Optional<Room> room = participatingRooms.stream().filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        if (room.isPresent()){
            room.get().addPeer(newPeer);
            if (room.get().getMembersNumber() == room.get().getOtherRoomMembers().size()) {
                String creator = room.get().getOtherRoomMembers().get(0).getUsername();
                CLI.appendNotification("You have been inserted in a new room by "+creator+"! The ID of the room is: "+roomID);
            }
        }
        else{
            throw new InvalidParameterException("There is no room with such UUID: " + roomID);
        }
    }

    public void close() {
        sender.stopSendingPendingPacketsAtFixedRate();
        if (sender.getSocket() != null && !sender.getSocket().isClosed()) {
            sender.getSocket().close();
        }
        if (listener.getSocket() != null && !listener.getSocket().isClosed()) {
            listener.getSocket().close();
        }
        inScanner.close();
    }

    private InetAddress extractBroadcastAddress(InetAddress ipAddress) throws UnknownHostException {
        byte[] addr = ipAddress.getAddress();
        byte[] mask = ipAddress instanceof java.net.Inet4Address ? new byte[] {(byte)255, (byte)255, (byte)255, (byte)0} : new byte[] {(byte)255, (byte)255, (byte)255, (byte)255, (byte)0, (byte)0, (byte)0, (byte)0};
        byte[] broadcast = new byte[addr.length];
        
        for (int i = 0; i < addr.length; i++) {
            broadcast[i] = (byte) (addr[i] | ~mask[i]);
        }
        
        return InetAddress.getByAddress(broadcast);
    }

    public int getAndIncrementSequenceNumber(){
        int result = sequenceNumber;
        sequenceNumber++;
        return result;
    }

    public String getProcessID(){
        return myself.getIdentifier().toString();
    }

    public void acknowledge(int sequenceNumber){
        sender.acknowledge(sequenceNumber);
    }

    public void sendPacket(Message message, Integer sequenceNumber) throws IOException {
        sender.sendPacket(message, sequenceNumber);
    }
}
