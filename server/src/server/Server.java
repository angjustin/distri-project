package server;

import client.*;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 2222;
    private static Map<String, ClientInfo> registeredClients = new HashMap<>();
    public static void main(String[] args) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println("Local Host IP Address: " + localHost.getHostAddress());

        // Get all IP addresses associated with the local host
        InetAddress[] allLocalAddresses = InetAddress.getAllByName(localHost.getHostName());
        System.out.println("All Local Host IP Addresses:");
        for (InetAddress address : allLocalAddresses) {
            System.out.println(" - " + address.getHostAddress());
        }
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server is running...");
            Storage storage = new Storage();

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                // Get the length of received data and only deserialize that portion
                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                Object request = Marshalling.deserialize(receivedData);
                System.out.println("address " + receivePacket.getAddress());
                System.out.println("port " + receivePacket.getPort());
                byte[] replyData;
                if (request instanceof ReadRequest){
                    replyData = Marshalling.serialize(storage.readBytes((ReadRequest) request));
                }
                else if (request instanceof WriteRequest) {
                    // TODO: add informing registered clients upon write
                    replyData = Marshalling.serialize(storage.writeBytes((WriteRequest) request));
                } else if (request instanceof PropertiesRequest) {
                    replyData = Marshalling.serialize(storage.getAttributes((PropertiesRequest) request));
                } else if (request instanceof RegisterRequest){
                    // TODO: handle callback request + reply
                    System.out.println("Received register request");
                    continue;
                } else {
                    System.err.println("Unknown request type.");
                    replyData = Marshalling.serialize(new Reply());
                }

                DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}