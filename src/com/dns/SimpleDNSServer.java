import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class SimpleDNSServer {
    private static final Map<String, String> DOMAIN_MAP = new HashMap<>();
    private static InetAddress GOOGLE_DNS;

    static {
        try {
            GOOGLE_DNS = InetAddress.getByName("8.8.8.8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        DOMAIN_MAP.put("mywebsite.com", "192.168.1.100");
        DOMAIN_MAP.put("myservice.org", "10.0.0.5");
        DOMAIN_MAP.put("test.local", "127.0.0.1");
    }

    public static void main(String[] args) throws IOException {
        try (DatagramSocket socket = new DatagramSocket(53)) {
            byte[] buffer = new byte[512];

            System.out.println("DNS Server is running on port 53...");

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                ByteBuffer dnsMessage = ByteBuffer.wrap(request.getData(), 0, request.getLength());
                dnsMessage.order(ByteOrder.BIG_ENDIAN);

                // Parse the Question to get the domain name
                dnsMessage.position(12);
                StringBuilder qName = new StringBuilder();
                int length;
                while ((length = Byte.toUnsignedInt(dnsMessage.get())) != 0) {
                    byte[] labelBytes = new byte[length];
                    dnsMessage.get(labelBytes);
                    qName.append(new String(labelBytes)).append(".");
                }
                String domainName = qName.toString().substring(0, qName.length() - 1);
                
                System.out.println("Received query for: " + domainName);
                
                String ipAddress = DOMAIN_MAP.get(domainName);
                if (ipAddress != null) {
                    // Domain found locally, send a successful response
                    sendLocalResponse(socket, request, dnsMessage, ipAddress);
                } else {
                    // Domain not found locally, act as a recursive resolver
                    sendRecursiveQuery(socket, request);
                }
            }
        }
    }

    private static void sendLocalResponse(DatagramSocket socket, DatagramPacket request, ByteBuffer dnsMessage, String ipAddress) throws IOException {
        ByteBuffer response = ByteBuffer.allocate(512);
        response.order(ByteOrder.BIG_ENDIAN);
        
        response.putShort(dnsMessage.getShort(0));
        response.putShort((short) 0x8180);
        response.putShort((short) 1);
        response.putShort((short) 1);
        response.putShort((short) 0);
        response.putShort((short) 0);

        response.put(request.getData(), 12, request.getLength() - 12);

        response.putShort((short) 0xC00C);
        response.putShort((short) 1);
        response.putShort((short) 1);
        response.putInt(60);
        response.putShort((short) 4);

        String[] ipParts = ipAddress.split("\\.");
        for (String part : ipParts) {
            response.put((byte) Integer.parseInt(part));
        }
        System.out.println("Responding with local IP: " + ipAddress);
        
        byte[] responseData = new byte[response.position()];
        response.rewind();
        response.get(responseData);

        DatagramPacket responsePacket = new DatagramPacket(
            responseData, 
            responseData.length, 
            request.getAddress(), 
            request.getPort()
        );

        socket.send(responsePacket);
    }

    private static void sendRecursiveQuery(DatagramSocket socket, DatagramPacket request) throws IOException {
        System.out.println("Domain not found locally. Forwarding query to " + GOOGLE_DNS);

        DatagramPacket forwardPacket = new DatagramPacket(
            request.getData(), 
            request.getLength(), 
            GOOGLE_DNS, 
            53
        );
        socket.send(forwardPacket);
        
        byte[] responseBuffer = new byte[512];
        DatagramPacket receivedFromGoogle = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(receivedFromGoogle);

        DatagramPacket responsePacket = new DatagramPacket(
            receivedFromGoogle.getData(),
            receivedFromGoogle.getLength(),
            request.getAddress(),
            request.getPort()
        );
        socket.send(responsePacket);
        System.out.println("Forwarded response from Google to client.");
    }
}