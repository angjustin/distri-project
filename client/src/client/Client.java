package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import server.ClientInfo;
import server.Reply;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Client {
    private static final String SERVER_IP = "192.168.88.245";
    private static final int SERVER_PORT = 2222;
    private static volatile boolean isMonitoring = false;

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
                        System.out.println("Enter monitor interval (in seconds): ");
                        int interval = InputManager.getInt();
                        RegisterRequest registerRequest = new RegisterRequest(filePath, interval);
                        registerRequest.print();
                        byte[] sendBuffer = Marshalling.serialize(registerRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        System.out.println("1 " + sendPacket.getAddress() + sendPacket.getPort());
                        socket.send(sendPacket);
                        // sleep to simulate being blocked from issuing register request
                        // TODO: handle printing of reply e.g. monitoring in progress...
                        startMonitoring();
                        long startTime = System.currentTimeMillis();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                stopMonitoring();
                            }
                        }, interval * 1000L);
                        while (isMonitoring){
                            byte[] receiveBuffer = new byte[1024];
                            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                            long timeElapsed = System.currentTimeMillis() - startTime;
                            if (timeElapsed > (interval * 1000L)) {
                                break;
                            } else {
                                socket.setSoTimeout((int) (interval * 1000L - timeElapsed));
                            }
                            try {
                                socket.receive(receivePacket);

                                // Process received data from the server
                                // Example: Print received data
//                                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
//                                System.out.println("Received: " + receivedData);

                            } catch (SocketTimeoutException e) {
                                // Handle timeout (no datagram received within the timeout period)
                                // Example: Print a message indicating no data received
                                System.out.println("No further updates to file.");
                            }
                        }
                        socket.setSoTimeout(Integer.MAX_VALUE);
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
        }
    }

    private static void stopMonitoring() {
        isMonitoring = false;
    }

    private static void startMonitoring() {
        isMonitoring = true;
    }
}