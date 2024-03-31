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
            System.out.println();
            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                // Get the length of received data and only deserialize that portion
                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                Object request = Marshalling.deserialize(receivedData);
                byte[] replyData;

                System.out.println("Received request...");
                switch (request) {
                    case ReadRequest readRequest -> {
                        replyData = Marshalling.serialize(storage.readBytes(readRequest));
                        readRequest.print();
                    }


                    case WriteRequest writeRequest -> {
                        replyData = Marshalling.serialize(storage.writeBytes(writeRequest));
                        writeRequest.print();
                    }

                    case PropertiesRequest propertiesRequest -> {
                        replyData = Marshalling.serialize(storage.getProperties(propertiesRequest));
                        propertiesRequest.print();
                    }

                    case FileRequest fileRequest -> {
                        replyData = Marshalling.serialize(storage.getFile(fileRequest));
                        fileRequest.print();
                    }

                    case null, default -> {
                        System.err.println("Unknown request type.");
                        replyData = Marshalling.serialize(new Reply());
                    }
                }

                System.out.println("Sending reply...");
                Reply reply = (Reply) Marshalling.deserialize(replyData);
                reply.print();

                DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}