package test;

import org.junit.Test;
import project.Client;
import project.Communication.Messages.MessageCausalityStatus;
import project.Communication.Messages.RoomTextMessage;
import project.Communication.NetworkUtils;
import project.Model.Peer;
import project.Model.Room;
import project.Model.RoomText;
import project.Model.VectorClock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CausalityTest {

    private final Client client;
    private final InetAddress ip;
    private final Peer peer2;
    private final Peer peer3;
    private final Room room;

    public CausalityTest() throws UnknownHostException {
        try {
            client = new Client("test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ip = InetAddress.getByName("127.0.0.1");
        peer2 = new Peer("test", ip);
        peer3 = new Peer("test", ip);
        Set<Peer> peers = new HashSet<>();
        peers.add(client.getPeerData());
        peers.add(peer2);
        peers.add(peer3);
        room = new Room("test", peers, InetAddress.getLocalHost());
    }

    @Test
    public void testVectorClockAccepted1() throws IOException {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 0);
        rvc.add(peer2.getIdentifier(), 0);
        rvc.add(peer3.getIdentifier(), 0);
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.ACCEPTED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockAccepted2() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 0);
        rvc.add(peer2.getIdentifier(), 0);
        rvc.add(peer3.getIdentifier(), 0);
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 2);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.ACCEPTED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockAccepted3() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 0);
        rvc.add(peer2.getIdentifier(), 2);
        rvc.add(peer3.getIdentifier(), 0);
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.ACCEPTED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockQueued1() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 0);
        rvc.add(peer2.getIdentifier(), 0);
        rvc.add(peer3.getIdentifier(), 0);
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 1);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.QUEUED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockDiscarded1() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 0);
        rvc.add(peer2.getIdentifier(), 1);
        rvc.add(peer3.getIdentifier(), 0);
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.DISCARDED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockDiscarded2() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 1); // P2
        rvc.add(peer2.getIdentifier(), 1); // P1
        rvc.add(peer3.getIdentifier(), 0); // P3
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 0);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.DISCARDED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

    @Test
    public void testVectorClockDiscarded3() throws Exception {
        VectorClock rvc = new VectorClock();
        rvc.add(client.getPeerData().getIdentifier(), 1); // P2
        rvc.add(peer2.getIdentifier(), 1); // P1
        rvc.add(peer3.getIdentifier(), 1); // P3
        VectorClock mvc = new VectorClock();
        mvc.add(client.getPeerData().getIdentifier(), 1);
        mvc.add(peer2.getIdentifier(), 1);
        mvc.add(peer3.getIdentifier(), 0);
        RoomTextMessage msg = new RoomTextMessage(mvc, peer2.getIdentifier(), ip, NetworkUtils.MULTICAST_PORT_NUMBER,
                new RoomText(room.getIdentifier(), peer2, "test"), UUID.randomUUID());
        assertEquals(MessageCausalityStatus.DISCARDED, client.testCausality(rvc, msg));
        client.getUnicastListener().close();
    }

}
