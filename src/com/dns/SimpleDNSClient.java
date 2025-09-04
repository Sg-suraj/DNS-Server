import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class SimpleDNSClient {
    public static void main(String[] args) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            Scanner scanner = new Scanner(System.in);
            String domain = scanner.nextLine();

            // --- 1. Construct the DNS Query Message ---
            ByteBuffer query = ByteBuffer.allocate(512);
            query.order(ByteOrder.BIG_ENDIAN);

            // Header Section
            query.putShort((short) 1);
            query.putShort((short) 0x0100);
            query.putShort((short) 1);
            query.putShort((short) 0);
            query.putShort((short) 0);
            query.putShort((short) 0);

            // Question Section
            for (String label : domain.split("\\.")) {
                query.put((byte) label.length());
                query.put(label.getBytes());
            }
            query.put((byte) 0);

            query.putShort((short) 1);
            query.putShort((short) 1);

            byte[] queryData = new byte[query.position()];
            query.rewind();
            query.get(queryData);

            // --- 2. Send the Query to the Server ---
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 53;
            DatagramPacket sendPacket = new DatagramPacket(queryData, queryData.length, serverAddress, serverPort);
            clientSocket.send(sendPacket);

            // --- 3. Receive the Response ---
            byte[] responseBuffer = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            clientSocket.receive(receivePacket);
            ByteBuffer response = ByteBuffer.wrap(receivePacket.getData(), 0, receivePacket.getLength());
            response.order(ByteOrder.BIG_ENDIAN);

            // Read the Header to check the response code
            response.position(2);
            short flags = response.getShort();
            int rCode = flags & 0x0F;
            
            // Skip the rest of the header and question sections to get to the answer section
            response.position(12);
            int length;
            while ((length = Byte.toUnsignedInt(response.get())) != 0) {
                response.position(response.position() + length);
            }
            response.position(response.position() + 4);

            if (rCode == 0) {
                // Skip the answer header
                response.position(response.position() + 12);
                
                // Get the IP address
                byte[] ipAddress = new byte[4];
                response.get(ipAddress);
                
                System.out.println("Resolved IP Address: " +
                    Byte.toUnsignedInt(ipAddress[0]) + "." +
                    Byte.toUnsignedInt(ipAddress[1]) + "." +
                    Byte.toUnsignedInt(ipAddress[2]) + "." +
                    Byte.toUnsignedInt(ipAddress[3])
                );
            } else if (rCode == 3) {
                System.out.println("Error: Domain not found (NXDOMAIN).");
            } else {
                System.out.println("Error: Received an unexpected error code: " + rCode);
            }

        } catch (IOException e) {
            System.out.println("Error: An IOException occurred in the client.");
            e.printStackTrace();
        }
    }
}