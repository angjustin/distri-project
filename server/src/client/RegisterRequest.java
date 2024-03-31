package client;

import java.security.SecureRandom;

import server.ClientInfo;

public class RegisterRequest {

    public static final byte code = 3;
    private String filePath;
    private int monitorInterval;
    private ClientInfo clientInfo;
    private final long id;

    public RegisterRequest(String filePath, int monitorInterval, ClientInfo clientInfo){
        this.filePath = filePath;
        this.monitorInterval = monitorInterval;
        this.clientInfo = clientInfo;
        this.id = new SecureRandom().nextLong();
    }

    public RegisterRequest(String filePath, int monitorInterval){
        this.filePath = filePath;
        this.monitorInterval = monitorInterval;
        this.clientInfo = null;
        this.id = new SecureRandom().nextLong();
    }
    public long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void print() {
        System.out.println("Type: Register Request");
        System.out.println("Path: " + filePath);
        System.out.println("Monitor Interval: " + monitorInterval);
        System.out.println("Client Info" + clientInfo.toString());
        System.out.println();
    }

}
