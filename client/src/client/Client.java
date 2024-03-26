package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

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
                System.out.println("4: Terminate program.");

                // Read user input
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                // Process user choice
                switch (choice) {
                    case 1 -> {
                        System.out.println("Service 1: Read content of a file by specifying pathname, offset, and number of bytes.");
                        System.out.print("Enter file pathname: ");
                        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                        String filePath = userInput.readLine();
                        System.out.print("Enter offset (in bytes): ");
                        int offset = Integer.parseInt(userInput.readLine());
                        System.out.print("Enter number of bytes to read: ");
                        int bytesToRead = Integer.parseInt(userInput.readLine());

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
                        String response = new String(receivePacket.getData(),   0, receivePacket.getLength());
                        System.out.println("Response from server: " + response);
                    }
                    case 2 -> {
                        System.out.println("Service 2: Insert content into a file by specifying pathname, offset, and sequence of bytes.");
                        System.out.print("Enter file pathname: ");
                        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                        String filePath = userInput.readLine();
                        System.out.print("Enter offset (in bytes): ");
                        int offset = Integer.parseInt(userInput.readLine());
                        System.out.print("Enter sequence of bytes to write: ");
                        String stringToWrite = userInput.readLine();
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
                        String response = new String(receivePacket.getData(),   0, receivePacket.getLength());
                        System.out.println("Response from server: " + response);

                    }

                    case 3 ->
                            System.out.println("Service 3: Monitor updates made to a file's content for a designated time period.");
                    case 4 -> {
                        System.out.println("Terminating program.");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please choose a valid option.");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}