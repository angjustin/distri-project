package server;

import client.*;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server {
    private static final int PORT = 2222;
    private static Map<ClientInfo, RegisterRequest> registeredClients = new HashMap<>();
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server is running...");
            Storage storage = new Storage();

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                // Get the length of received data and only deserialize that portion
                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                Object request = Marshalling.deserialize(receivedData);
                byte[] replyData;
                if (request instanceof ReadRequest){
                    replyData = Marshalling.serialize(storage.readBytes((ReadRequest) request));
                }
                else if (request instanceof WriteRequest) {
                    // TODO: add informing registered clients upon write
                    replyData = Marshalling.serialize(storage.writeBytes((WriteRequest) request));
                    // remove any expired entries
                    removeAllExpired();
                    // notify any subscribed clients
                    System.out.println("Notifying");
                    for (Map.Entry<ClientInfo, RegisterRequest> entry : registeredClients.entrySet()){
                        if (Objects.equals(((WriteRequest) request).getPath(), entry.getValue().getPath())){
                            Reply notif = storage.getUpdatedFile(entry.getValue());
                            byte[] notifBytes = Marshalling.serialize(notif);
                            InetAddress address = entry.getKey().getAddress();
                            int port = entry.getKey().getPort();
                            DatagramPacket sendPacket = new DatagramPacket(notifBytes, notifBytes.length, address, port);
                            socket.send(sendPacket);
                        }
                    }
                } else if (request instanceof PropertiesRequest) {
                    replyData = Marshalling.serialize(storage.getAttributes((PropertiesRequest) request));
                } else if (request instanceof RegisterRequest){
                    System.out.println("Received register request");
                    replyData = Marshalling.serialize(storage.registerCheck((RegisterRequest) request));
                    registerClient((RegisterRequest) request, new ClientInfo(clientAddress, clientPort));
                } else {
                    System.err.println("Unknown request type.");
                    replyData = Marshalling.serialize(new Reply());
                }

                DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void registerClient(RegisterRequest req, ClientInfo info){
        removeAllExpired();
        registeredClients.put(info, req);
        System.out.println("Added: " + info);
        System.out.println("All registered: " + registeredClients);
    }

    private static void deregisterClient(ClientInfo info){
        registeredClients.remove(info);
        System.out.println("Removed: " + info);
    }

    private static void removeAllExpired(){
        System.out.println("Checking expiry");
        for (Map.Entry<ClientInfo, RegisterRequest> entry : registeredClients.entrySet()){
            Instant expiry = entry.getKey().getStartTime().plusSeconds(entry.getValue().getMonitorInterval());
            // current time is past the expiry time
            if (Instant.now().compareTo(expiry) > 0){
                System.out.println("Removing: " + entry.getKey());
                deregisterClient(entry.getKey());
            }
        }
    }

}