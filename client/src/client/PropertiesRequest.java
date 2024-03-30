package client;

import java.security.SecureRandom;

public class PropertiesRequest {
    public static final byte code = 4;
    private final String path;
    private final long id;
    public PropertiesRequest(String path, long id) {
        this.path = path;
        this.id = id;
    }

    public PropertiesRequest(String path) {
        this.path = path;
        this.id = new SecureRandom().nextLong();
    }

    public String getPath() {
        return path;
    }

    public long getId() {
        return id;
    }
    public void print() {
        System.out.println("Type: Attribute Request");
        System.out.println("Path: " + path);
        System.out.println("ID: " + id);
        System.out.println();
    }
}
