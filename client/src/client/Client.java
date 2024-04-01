package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import server.Reply;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 2222;
    private static volatile boolean isMonitoring = false;

    private static final int FRESHNESS = 5000;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            Cache cache = new Cache(FRESHNESS);

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

                        // Check cache for file
                        if (cache.hasRecord(filePath)) {
                            if (!cache.isRecordStale(filePath)) {
                                System.out.println("Cache still fresh, reading from cached file...");
                                cache.printFile(readRequest);
                                break;
                            } else {
                                System.out.println("Record stale.");
                            }
                        } else {
                            System.out.println("File does not exist in cache.");
                        }

                        // Create properties request
                        PropertiesRequest propRequest = new PropertiesRequest(filePath);
                        byte[] sendBuffer = Marshalling.serialize(propRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);
                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);
                        byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                        Reply reply = (Reply) Marshalling.deserialize(receivedData);

                        if (reply.getResult() != PropertiesRequest.code) {
                            reply.printClient();
                            break;
                        }

                        // Get file properties from server
                        Cache.Record record = (Cache.Record) Marshalling.deserialize(reply.getBody());
                        if (cache.isRecordValid(filePath, record)) {
                            System.out.println("Refreshed cache, reading from cached file...");
                            cache.refreshRecord(filePath, record);
                            cache.printFile(readRequest);
                            break;
                        }

                        // Create file request
                        FileRequest fileRequest = new FileRequest(filePath);
                        sendBuffer = Marshalling.serialize(fileRequest);

                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);

                        // Receive from server
                        receiveBuffer = new byte[1024];
                        receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);
                        receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                        reply = (Reply) Marshalling.deserialize(receivedData);

                        if (reply.getResult() != FileRequest.code) {
                            reply.printClient();
                            break;
                        }

                        // Get file from server
                        byte[] fileBytes = reply.getBody();

                        cache.addFile(filePath, record, fileBytes);
                        cache.printFile(readRequest);
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

                        // Delete cached file
                        cache.deleteFile(writeRequest.getPath());

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

                        byte[] sendBuffer = Marshalling.serialize(registerRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                        socket.send(sendPacket);
                        // sleep to simulate being blocked from issuing register request
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
                                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                Reply reply = (Reply) Marshalling.deserialize(receivedData);

                                reply.printClient();
                                // if file does not exist
                                if (reply.getResult() == (byte)10) {
                                    break;
                                }

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