package project.Communication;

import java.net.*;
import java.util.Enumeration;
import java.util.Random;

/**
 * This class provides a lot of utility functions to handle any type of communication in the LAN scenario.
 */
public class NetworkUtils {

    /**
     * The port number all peers agree to receiving unicast or broadcast messages from.
     */
    public static final int UNICAST_PORT_NUMBER = 9999;

    /**
     * The port number all peers agree to receiving multicast messages from.
     */
    public static final int MULTICAST_PORT_NUMBER = 12345;

    /**
     * The maximum payload dimension for the UDP packets used to exchange messages. In the conceived use cases for
     * this application, it should never be exceeded.
     */
    public static final int BUF_DIM = 60000;

    /**
     * Given an unicast IP address, it returns the corresponding broadcast address.
     *
     * @param ip The unicast IP address.
     * @return The broadcast IP address.
     * @throws UnknownHostException If the IP address of a host could not be determined.
     * @throws SocketException If there's an error accessing or creating a socket.
     */
    public static InetAddress getBroadcastAddress(InetAddress ip) throws UnknownHostException, SocketException {

        // gets the subnet mask
        InetAddress mask = getSubnetMask(ip);

        // get IP address and subnet mask as byte arrays
        byte[] ipBytes = ip.getAddress();
        byte[] maskBytes = mask.getAddress();

        // compute broadcast address
        byte[] broadcastBytes = new byte[ipBytes.length];
        for (int i = 0; i < ipBytes.length; i++) {
            broadcastBytes[i] = (byte) (ipBytes[i] | (~maskBytes[i] & 0xFF));
        }

        // returns the broadcast IP address as InetAddress
        return InetAddress.getByAddress(broadcastBytes);
    }

    /**
     * Given an IP address, it returns the subnet mask of the network.
     *
     * @param targetIP The IP address to extract the subnet mask from.
     * @return The subnet mask of the given IP address.
     * @throws SocketException If there's an error accessing or creating a socket.
     * @throws UnknownHostException If the IP address of a host could not be determined.
     */
    private static InetAddress getSubnetMask(InetAddress targetIP) throws SocketException, UnknownHostException {
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
        return InetAddress.getByName(subnetMask);
    }

    /**
     * Gets the subnet mask from prefix length and returns it as a string.
     *
     * @param prefixLength The prefix length to return the subnet mask from.
     * @return The subnet mask as a string.
     */
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

    /**
     * Returns a random multicast IP address that is currently available.
     *
     * @return A random and available multicast IP address.
     * @throws UnknownHostException If the IP address of a host could not be determined.
     */
    public static InetAddress generateRandomMulticastAddress() throws UnknownHostException {
        Random random = new Random();

        // generates a random number in the range 224-239
        int firstByte = random.nextInt(16) + 224;

        // generates three random numbers in the range 0-255
        int secondByte = random.nextInt(256);
        int thirdByte = random.nextInt(256);
        int fourthByte = random.nextInt(256);

        // returns the randomly generated multicast address in the correct format
        return InetAddress.getByName(String.format("%d.%d.%d.%d", firstByte, secondByte, thirdByte, fourthByte));
    }

    /**
     * Returns a network interface that is up and available, and that supports IPv4.
     *
     * @return A network interface that is up and available, and that supports IPv4.
     */
    public static NetworkInterface getAvailableMulticastIPv4NetworkInterface() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp() && !networkInterface.isVirtual()) {
                    // if the interface supports both multicast and IPv4, it is returned
                    if (networkInterface.supportsMulticast() && hasIPv4Address(networkInterface)) {
                        return networkInterface;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage());
        }

        return null; // in case no network interface is available
    }

    /**
     * Returns whether the given network interface supports IPv4 addresses.
     *
     * @param networkInterface The network interface to test.
     * @return Whether the given network interface supports IPv4 addresses.
     */
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
