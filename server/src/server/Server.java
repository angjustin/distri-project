package server;

import client.*;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;

public class Server {
    private static final int PORT = 2222;
    // hashmap storing clients registered for file updates
    private static Map<String, ClientInfo> registeredClients = new HashMap<>();
    // hashmap storing processed requests
    private static Map<Long, Reply> processedRequests = new HashMap<>();

    // At-most-once semantics: Duplicate filtering and retransmission of reply
    private static final Boolean isAtMostOnce = false;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server is running...");
            Storage storage = new Storage();
            System.out.println();
            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                // Get the length of received data and only deserialize that portion
                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                // Get client information (address and port)
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                // Deserialise reply to get request type
                Object request = Marshalling.deserialize(receivedData);
                byte[] replyData;
                System.out.println("Received request...");

                Long requestId = null;

                switch (request) {
                    case ReadRequest readRequest -> {
                        requestId = readRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        else{
                            replyData = Marshalling.serialize(storage.readBytes(readRequest));
                            readRequest.print();
                        }
                    }

                    case WriteRequest writeRequest -> {
                        requestId = writeRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        replyData = Marshalling.serialize(storage.writeBytes((WriteRequest) request));
                        // remove any expired entries
                        removeAllExpired();
                        // notify any subscribed clients
                        System.out.println("Notifying");
                        for (Map.Entry<String, ClientInfo> entry : registeredClients.entrySet()){
                            if (Objects.equals(((WriteRequest) request).getPath(), entry.getValue().getRegisterRequest().getPath())){
                                Reply notif = storage.getUpdatedFile(entry.getValue().getRegisterRequest());
                                notif.print();
                                byte[] notifBytes = Marshalling.serialize(notif);
                                InetAddress address = entry.getValue().getAddress();
                                int port = entry.getValue().getPort();
                                DatagramPacket sendPacket = new DatagramPacket(notifBytes, notifBytes.length, address, port);
                                socket.send(sendPacket);
                            }
                        }
                        writeRequest.print();
                    }

                    case PropertiesRequest propertiesRequest -> {
                        requestId = propertiesRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            System.out.println("Sending reply...");
                            reply.print();
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        replyData = Marshalling.serialize(storage.getProperties(propertiesRequest));
                        propertiesRequest.print();
                    }

                    case RegisterRequest registerRequest -> {
                        requestId = registerRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        Reply reply = storage.registerCheck((RegisterRequest) request);
                        replyData = Marshalling.serialize(reply);
                        if (reply.getResult() == RegisterRequest.code) {
                            ClientInfo clientInfo = new ClientInfo(clientAddress, clientPort, registerRequest);
                            registerClient(clientInfo.printAddressPort(), clientInfo );
                        }
                        registerRequest.print();
                    }

                    case FileRequest fileRequest -> {
                        requestId = fileRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            System.out.println("Sending reply...");
                            reply.print();
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        replyData = Marshalling.serialize(storage.getFile(fileRequest));
                        fileRequest.print();
                    }

                    case DeleteRequest deleteRequest -> {
                        requestId = deleteRequest.getId();
                        if (isAtMostOnce && isDuplicateRequest(requestId)){
                            // Retransmit reply
                            Reply reply = processedRequests.get(requestId);
                            System.out.println("Sending reply...");
                            reply.print();
                            replyData = Marshalling.serialize(reply);
                            DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                            socket.send(sendPacket);
                            continue;
                        }
                        Reply reply = storage.deleteFile((DeleteRequest) request);
                        replyData = Marshalling.serialize(reply);
                        deleteRequest.print();
                    }

                    case null, default -> {
                        System.err.println("Unknown request type.");
                        replyData = Marshalling.serialize(new Reply());
                    }
                }

                Reply reply = (Reply) Marshalling.deserialize(replyData);


                // Mark request as processed
                // EXPERIMENT: Simulate lost of packet from server to client and do not send to client for first request
                if (!isDuplicateRequest(requestId)){
                    markRequestAsProcessed(requestId, reply);
                }
                // Send duplicate requests to client
                else {
                    System.out.println("Sending reply 2");
                    reply.print();
                    DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void registerClient(String client, ClientInfo info){
        // remove any expired subscriptions
        removeAllExpired();
        // check if client is already registered
        if (registeredClients.containsKey(client)) {
            System.out.println("Client is already registered");
            return;
        }
        registeredClients.put(client, info);
        System.out.println("Added: " + client);
        System.out.println("All registered: " + registeredClients);
    }

    private static void deregisterClient(String info){
        registeredClients.remove(info);
        System.out.println("Removed: " + info);
    }

    private static void removeAllExpired(){
        System.out.println("Removing expired subscribers");
        if (registeredClients.isEmpty()){
            return;
        }
        for (Map.Entry<String, ClientInfo> entry : registeredClients.entrySet()){
            // current time is past the expiry time
            if (Instant.now().compareTo(entry.getValue().getExpiry()) > 0){
                deregisterClient(entry.getKey());
            }
        }
    }

    private static boolean isDuplicateRequest(Long requestId){
        return processedRequests.containsKey(requestId);
    }

    private static void markRequestAsProcessed(Long requestId, Reply reply){
        if (!processedRequests.containsKey(requestId)){
            processedRequests.put(requestId, reply);
            System.out.println("Mark request as processed");
        }
        else{
            System.out.println("Request: " + requestId + " already processed.");
        }
    }

}