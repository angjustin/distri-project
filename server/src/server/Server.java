package server;

import client.*;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {
    private static final int PORT = 2222;
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
                Object request = Marshalling.deserialize(receivedData);
                byte[] replyData;
                switch (request) {
                    case ReadRequest readRequest -> replyData = Marshalling.serialize(storage.readBytes(readRequest));
                    case WriteRequest writeRequest ->
                            replyData = Marshalling.serialize(storage.writeBytes(writeRequest));
                    case PropertiesRequest propertiesRequest ->
                            replyData = Marshalling.serialize(storage.getProperties(propertiesRequest));
                    case FileRequest fileRequest -> replyData = Marshalling.serialize(storage.getFile(fileRequest));
                    case null, default -> {
                        System.err.println("Unknown request type.");
                        replyData = Marshalling.serialize(new Reply());
                    }
                }

                DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}