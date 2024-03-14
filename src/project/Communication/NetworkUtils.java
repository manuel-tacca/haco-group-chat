package project.Communication;

import java.net.*;
import java.util.Enumeration;
import java.util.Random;
import java.io.IOException;

public class NetworkUtils {

    public static final int UNICAST_PORT_NUMBER = 9999;
    public static final int MULTICAST_PORT_NUMBER = 10001;

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

    public static int generateRandomPort() {
        Random random = new Random();
        int port;
        // Genera un numero casuale nell'intervallo delle porte valide (1024-65535)
        do {
            port = random.nextInt((65535 - 1024) + 1) + 1024;
        }
        while(!isPortAvailable(port));
        return port;
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Se non viene sollevata un'eccezione, la porta è disponibile
            return true;
        } catch (IOException e) {
            // Se viene sollevata un'eccezione, la porta è già in uso
            return false;
        }
    }

    /*
    public Boolean checkCorrectIpFormat(String ipAddress) {
        String IP_ADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

        String[] octets = ipAddress.split("\\.");
        int firstOctet = Integer.parseInt(octets[0]);

        if(!(firstOctet >= 224 && firstOctet <= 239)) {
            CLI.printError("The provided address is not in the correct format!");
            return false;
        }
        return true;
    }

    public boolean checkCorrectPortFormat(String portS) {
        int port = Integer.parseInt(portS);
        return port >= 1024 && port <= 4151;
    }
    */

}
