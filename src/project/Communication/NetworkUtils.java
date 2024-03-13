package project.Communication;

import java.net.*;
import java.util.Enumeration;

public class NetworkUtils {

    public static InetAddress extractBroadcastAddress(InetAddress ip) throws UnknownHostException, SocketException {

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

}
