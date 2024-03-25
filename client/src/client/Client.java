package client;

import java.io.*;
import java.net.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 2222;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            while (true) {
                System.out.print("Enter file pathname: ");
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                String filePath = userInput.readLine();
                System.out.print("Enter offset (in bytes): ");
                int offset = Integer.parseInt(userInput.readLine());
                System.out.print("Enter number of bytes to read: ");
                int bytesToRead = Integer.parseInt(userInput.readLine());

                // Create ReadRequest object
                ReadRequest readRequest = new ReadRequest(filePath,offset,bytesToRead);
                // Serialize ReadRequest object
                byte[] sendBuffer = Marshalling.serialize(readRequest);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Response from server: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}