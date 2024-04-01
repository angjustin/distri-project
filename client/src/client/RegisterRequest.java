package client;

import java.security.SecureRandom;

public class RegisterRequest {

    public static final byte code = 3;
    private final String filePath;
    private final int monitorInterval;
    private final long id;

    public RegisterRequest(String filePath, int monitorInterval){
        this.filePath = filePath;
        this.monitorInterval = monitorInterval;
        this.id = new SecureRandom().nextLong();
    }

    public RegisterRequest(String filePath, int monitorInterval, long id){
        this.filePath = filePath;
        this.monitorInterval = monitorInterval;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return filePath;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void print() {
        System.out.println("Type: Register Request");
        System.out.println("Path: " + filePath);
        System.out.println("Monitor Interval: " + monitorInterval);
        System.out.println("ID: " + id);
        System.out.println();
    }

}
