package client;

import server.ClientInfo;

public class RegisterRequest {

    private String filePath;
    private int monitorInterval;
    private ClientInfo clientInfo;

    public RegisterRequest(String filePath, int monitorInterval, ClientInfo clientInfo){
        this.filePath = filePath;
        this.monitorInterval = monitorInterval;
        this.clientInfo = clientInfo;
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
