package server;

import client.Marshalling;
import client.ReadRequest;
import client.WriteRequest;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {
    private static final int PORT = 2222;
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                // Get the length of received data and only deserialize that portion
                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                Object request = Marshalling.deserialize(receivedData);

                if (request instanceof ReadRequest){
                    handleReadRequest((ReadRequest) request, receivePacket.getAddress(), receivePacket.getPort(), socket);
                }
                else if (request instanceof WriteRequest) {
                    handleWriteRequest((WriteRequest) request, receivePacket.getAddress(), receivePacket.getPort(), socket);
                } else {
                    System.err.println("Unknown request type.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleReadRequest(ReadRequest readRequest, InetAddress clientAddress, int clientPort, DatagramSocket socket) throws IOException {
        Storage store = new Storage();
        String response = new String(store.readBytes(readRequest).getBody());
        byte[] sendBuffer = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    private static void handleWriteRequest(WriteRequest writeRequest, InetAddress clientAddress, int clientPort, DatagramSocket socket) throws IOException {
         Storage store = new Storage();
         store.writeBytes(writeRequest);
         String response = "Write request handled successfully" ;
         byte[] sendBuffer = response.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
         socket.send(sendPacket);
    }
}