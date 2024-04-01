package client;

import java.security.SecureRandom;

public class DeleteRequest {

    public static final byte code = 5;
    private final String path;

    private final long id;

    public DeleteRequest(String path) {
        this.path = path;
        this.id = new SecureRandom().nextLong();
    }

    public DeleteRequest(String path, long id) {
        this.path = path;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public long getId() {
        return id;
    }

    public void print() {
        System.out.println("Type: Delete Request");
        System.out.println("File pathname: " + path);
        System.out.println("ID: " + id);
        System.out.println();
    }

}
