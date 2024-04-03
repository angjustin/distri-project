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
        // args: Server address (string), Server port (int), Freshness interval (int)
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            int serverPort = SERVER_PORT;
            int freshness = FRESHNESS;
            if (args.length == 3) {
                // if all arguments are present, set client settings to them
                serverAddress = InetAddress.getByName(args[0]);
                serverPort = Integer.parseInt(args[1]);
                freshness = Integer.parseInt(args[2]);
            }



            // intialise cache with defined freshness interval
            Cache cache = new Cache(freshness);
            int MAX_RETRIES = 5;
            int TIMEOUT_MILLISECONDS = 2500;

            while (true){
                // Display menu to choose the program
                System.out.println("Choose the program: ");
                System.out.println("1: Read content of a file by specifying the file pathname, offset, and number of bytes to read.");
                System.out.println("2: Insert content into a file by specifying the file pathname, offset, and sequence of bytes.");
                System.out.println("3: Monitor updates made to the content of a specified file for a designated time period.");
                System.out.println("4: Delete existing file by specifying the file pathname");
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

                        // Initialise variables to check if need to retransmit request messages
                        boolean responseReceived = false;
                        int retries = 0;

                        // Create properties request
                        PropertiesRequest propRequest = new PropertiesRequest(filePath);
                        // Serialise properties request
                        byte[] sendBuffer = Marshalling.serialize(propRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
                        // Initialise other variables
                        Reply reply = null;
                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        byte [] receivedData;
                        // / Retransmit request messages as a fault tolerance measure
                        while (!responseReceived && retries < MAX_RETRIES){
                            try{
                                socket.send(sendPacket);
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);
                                // Receive from server
                                socket.receive(receivePacket);
                                responseReceived = true;
                                receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                reply = (Reply) Marshalling.deserialize(receivedData);
                            }
                            catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                            break;
                        }
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

                        // Re-initialise variables to check if need to retransmit request messages
                        responseReceived = false;
                        retries = 0;

                        // Create file request
                        FileRequest fileRequest = new FileRequest(filePath);
                        // Serialise file request
                        sendBuffer = Marshalling.serialize(fileRequest);
                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);

                        // Retransmit request messages as a fault tolerance measure
                        while (!responseReceived  && retries < MAX_RETRIES){
                            try{
                                socket.send(sendPacket);
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);
                                // Receive from server
                                receiveBuffer = new byte[1024];
                                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                socket.receive(receivePacket);
                                // Indicate that response is received
                                responseReceived = true;
                                receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                reply = (Reply) Marshalling.deserialize(receivedData);
                                if (reply.getResult() != FileRequest.code) {
//                                  // reply.printClient();
                                    responseReceived = false;
                                    retries++;
                                }
                            }
                            catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                            break;
                        }
                        // Add to cache
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

                        // Initialise variables to check if need to retransmit request messages
                        boolean responseReceived = false;
                        int retries = 0;
                        byte [] sendBuffer = Marshalling.serialize(writeRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);

                        // Retransmit request messages as a fault tolerance measure
                        while (!responseReceived  && retries < MAX_RETRIES){
                            try{
                                // Serialize WriteRequest object
                                socket.send(sendPacket);

                                // Set a timeout for receiving response
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);

                                // Receive from server
                                byte[] receiveBuffer = new byte[1024];
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                socket.receive(receivePacket);
                                responseReceived = true;
                                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                Reply reply = (Reply) Marshalling.deserialize(receivedData);
                                reply.printClient();
                            }
                            catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                        }
                    }

                    case 3 -> {
                        System.out.println("Service 3: Monitor updates made to a file's content for a designated time period.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();
                        System.out.print("Enter monitor interval (in seconds): ");
                        int interval = InputManager.getInt();
                        RegisterRequest registerRequest = new RegisterRequest(filePath, interval);

                        // Initialise variables to check if need to retransmit request messages
                        boolean responseReceived = false;
                        int retries = 0;

                        byte[] sendBuffer = Marshalling.serialize(registerRequest);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);

                        // Retransmit request messages as a fault tolerance measure
                        while (!responseReceived  && retries < MAX_RETRIES){
                            try{
                                // Serialize WriteRequest object
                                socket.send(sendPacket);

                                // Set a timeout for receiving response
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);

                                // Receive from server
                                byte[] receiveBuffer = new byte[1024];
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                socket.receive(receivePacket);
                                responseReceived = true;
                                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                Reply reply = (Reply) Marshalling.deserialize(receivedData);
                                reply.printClient();

                                // break if file not found
                                if (reply.getResult() == (byte)10) {
                                    break;
                                }

                                // sleep to simulate being blocked from issuing register request
                                startMonitoring();
                                long startTime = System.currentTimeMillis();
                                Timer timer = new Timer();
                                // turn off monitoring after interval ends
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        stopMonitoring();
                                    }
                                }, interval * 1000L);

                                // wait for updates
                                while (isMonitoring){
                                    receiveBuffer = new byte[1024];
                                    receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                    // set timeout to remaining monitoring time
                                    long timeElapsed = System.currentTimeMillis() - startTime;
                                    if (timeElapsed > (interval * 1000L)) {
                                        break;
                                    } else {
                                        socket.setSoTimeout((int) (interval * 1000L - timeElapsed));
                                    }
                                    // receive updates
                                    try {
                                        socket.receive(receivePacket);

                                        // Process received data from the server
                                        receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                        reply = (Reply) Marshalling.deserialize(receivedData);

                                        reply.printClient();

                                    } catch (SocketTimeoutException e) {
                                        // This timeout will occur at end of monitoring period
                                        System.out.println("No further updates to file.");
                                    }
                                }
                                // set timeout back to previous setting
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);


                            }
                            catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                        }
                    }

                    case 4 -> {
                        System.out.println("Service 4:  Delete existing file by specifying the file pathname.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();

                        // Create DeleteRequest object
                        DeleteRequest req = new DeleteRequest(filePath);

                        // Delete cached file
                        cache.deleteFile(req.getPath());

                        // Initialise variables to check if need to retransmit request messages
                        boolean responseReceived = false;
                        int retries = 0;

                        // Serialize DeleteRequest object
                        byte[] sendBuffer = Marshalling.serialize(req);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);

                        while (!responseReceived && retries < MAX_RETRIES){
                            try{
                                socket.send(sendPacket);
                                // Set a timeout for receiving response
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);
                                // Receive from server
                                byte[] receiveBuffer = new byte[1024];
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                socket.receive(receivePacket);
                                responseReceived = true;
                                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                Reply reply = (Reply) Marshalling.deserialize(receivedData);
                                reply.printClient();
                            }
                             catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                        }
                    }


                    case 5 -> {
                        System.out.println("Service 5: Get file properties by specifying the file pathname.");
                        System.out.print("Enter file pathname: ");
                        String filePath = InputManager.getString();

                        PropertiesRequest req = new PropertiesRequest(filePath);

                        // Initialise variables to check if need to retransmit request messages
                        boolean responseReceived = false;
                        int retries = 0;
                        byte[] sendBuffer = Marshalling.serialize(req);
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);

                        while (!responseReceived && retries < MAX_RETRIES){
                            try{
                                socket.send(sendPacket);
                                // Set a timeout for receiving response
                                socket.setSoTimeout(TIMEOUT_MILLISECONDS);

                                byte[] receiveBuffer = new byte[1024];
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                socket.receive(receivePacket);
                                responseReceived = true;
                                byte[] receivedData = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                                Reply reply = (Reply) Marshalling.deserialize(receivedData);

                                reply.printClient();
                            }
                            catch (SocketTimeoutException e){
                                retries++;
                                System.out.printf("Timeout occurred. Retrying request %d out of %d\n", retries, MAX_RETRIES);
                            }
                        }
                        if (!responseReceived) {
                            System.out.println("Maximum retries reached. Unable to complete request.");
                        }
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