package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import server.ClientInfo;
import server.Reply;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 2222;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            while (true){
                // Display menu to choose the program
                System.out.println("Choose the program: ");
                System.out.println("1: Read content of a file by specifying the file pathname, offset, and number of bytes to read.");
                System.out.println("2: Insert content into a file by specifying the file pathname, offset, and sequence of bytes.");
                System.out.println("3: Monitor updates made to the content of a specified file for a designated time period.");
                System.out.println("5: Get file properties by specifying the file pathname.");
                System.out.println("0: Terminate program.");

                // Read user input
                System.out.print("Enter your choice: ");
                int choice = InputManager.getInt();
                System.out.println();

                // Process user choice
                switch (choice) {
                    case 1 -> {
                        System.out.println("Service 1: Read content of a file by specifying pathname, offset, and number of bytes.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();
                        System.out.print("Enter offset (in bytes): ");
                        int offset = InputManager.getInt();
                        System.out.print("Enter number of bytes to read: ");

                        int bytesToRead = InputManager.getInt();
                        // Create ReadRequest object
                        ReadRequest readRequest = new ReadRequest(filePath, offset, bytesToRead);
                        // Serialize ReadRequest object
                        byte[] sendBuffer = Marshalling.serialize(readRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);

                        // Receive from server
                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);

                        byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                        Reply reply = (Reply) Marshalling.deserialize(receivedData);

                        reply.printClient();

                    }
                    case 2 -> {
                        System.out.println("Service 2: Insert content into a file by specifying pathname, offset, and sequence of bytes.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();
                        System.out.print("Enter offset (in bytes): ");
                        int offset = InputManager.getInt();
                        System.out.print("Enter sequence of bytes to write: ");
                        String stringToWrite = InputManager.getString();
                        byte [] bytesToWrite = stringToWrite.getBytes();

                        // Create WriteRequest object
                        WriteRequest writeRequest = new WriteRequest(filePath, offset, bytesToWrite);
                        // Serialize WriteRequest object
                        byte [] sendBuffer = Marshalling.serialize(writeRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);

                        // Receive from server
                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);

                        byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                        Reply reply = (Reply) Marshalling.deserialize(receivedData);

                        reply.printClient();

                    }

                    case 3 -> {
                        System.out.println("Service 3: Monitor updates made to a file's content for a designated time period.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();
                        System.out.println("Enter monitor interval (in ms): ");
                        int interval = InputManager.getInt();
                        RegisterRequest registerRequest = new RegisterRequest(filePath, interval, new ClientInfo(SERVER_IP, SERVER_PORT));
                        byte[] sendBuffer = Marshalling.serialize(registerRequest);
                        // sleep to simulate being blocked from issuing register request
                        Thread.sleep(interval);
                        // TODO: handle printing of reply e.g. monitoring in progress...
                    }

                    case 5 -> {
                        System.out.println("Service 5: Get file properties by specifying the file pathname.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();

                        PropertiesRequest req = new PropertiesRequest(filePath);
                        byte[] sendBuffer = Marshalling.serialize(req);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);

                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);

                        byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                        Reply reply = (Reply) Marshalling.deserialize(receivedData);

                        reply.printClient();

                    }
                    case 0 -> {
                        System.out.println("Terminating program.");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please choose a valid option.");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}