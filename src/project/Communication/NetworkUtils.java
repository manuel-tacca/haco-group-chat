package project.Communication;

import java.net.*;
import java.util.Enumeration;
import java.util.Random;

public class NetworkUtils {

    public static final int UNICAST_PORT_NUMBER = 9999;
    public static final int MULTICAST_PORT_NUMBER = 12345;
    public static final int BUF_DIM = 60000;

    public static InetAddress getBroadcastAddress(InetAddress ip) throws UnknownHostException, SocketException {

        InetAddress mask = InetAddress.getByName(getSubnetMask(ip));

        // Ottieni l'indirizzo IP e la subnet mask come array di byte
        byte[] ipBytes = ip.getAddress();
        byte[] maskBytes = mask.getAddress();

        // Calcola l'indirizzo di broadcast
        byte[] broadcastBytes = new byte[ipBytes.length];
        for (int i = 0; i < ipBytes.length; i++) {
            broadcastBytes[i] = (byte) (ipBytes[i] | (~maskBytes[i] & 0xFF));
        }

        // Converti l'indirizzo di broadcast in formato InetAddress e restituiscilo come stringa
        return InetAddress.getByAddress(broadcastBytes);
    }

    private static String getSubnetMask(InetAddress targetIP) throws SocketException {
        String subnetMask = null;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            for (InterfaceAddress addr : networkInterface.getInterfaceAddresses()) {
                if (addr.getAddress().equals(targetIP)) {
                    subnetMask = getSubnetMaskFromPrefixLength(addr.getNetworkPrefixLength());
                    break;
                }
            }
            if (subnetMask != null) {
                break;
            }
        }
        return subnetMask;
    }

    private static String getSubnetMaskFromPrefixLength(short prefixLength) {
        int mask = 0xffffffff << (32 - prefixLength);
        byte[] bytes = new byte[]{
                (byte) (mask >>> 24 & 0xff),
                (byte) (mask >>> 16 & 0xff),
                (byte) (mask >>> 8 & 0xff),
                (byte) (mask & 0xff)
        };
        try {
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get subnet mask from prefix length", e);
        }
    }

    public static InetAddress generateRandomMulticastAddress() throws UnknownHostException {
        Random random = new Random();

        // Genera un numero casuale nell'intervallo 224-239
        int firstByte = random.nextInt(16) + 224;

        // Genera tre numeri casuali nell'intervallo 0-255
        int secondByte = random.nextInt(256);
        int thirdByte = random.nextInt(256);
        int fourthByte = random.nextInt(256);

        // Crea e restituisce l'indirizzo IP multicast casuale
        return InetAddress.getByName(String.format("%d.%d.%d.%d", firstByte, secondByte, thirdByte, fourthByte));
    }

    public static NetworkInterface getAvailableMulticastIPv4NetworkInterface() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp() && !networkInterface.isVirtual()) {
                    // Se l'interfaccia supporta i multicast e utilizza IPv4, la restituisce
                    if (networkInterface.supportsMulticast() && hasIPv4Address(networkInterface)) {
                        return networkInterface;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace(); // Gestisci l'eccezione in base alle tue esigenze
        }

        return null; // Nessuna interfaccia di rete disponibile per ricevere messaggi multicast IPv4
    }

    private static boolean hasIPv4Address(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address instanceof Inet4Address) {
                return true;
            }
        }
        return false;
    }

}
