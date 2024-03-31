package client;

import java.security.SecureRandom;

public class FileRequest {
    public static final byte code = 6;
    private final String path;
    private final long id;
    public FileRequest(String path, long id) {
        this.path = path;
        this.id = id;
    }

    public FileRequest(String path) {
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
