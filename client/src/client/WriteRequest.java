package client;

import java.security.SecureRandom;
import java.util.Arrays;

public final class WriteRequest {
    public static final byte code = 2;
    private final String path;
    private final int offset;
    private final byte[] input;
    private final long id;

    public WriteRequest(String path, int offset, byte[] input, long id) {
        this.path = path;
        this.offset = offset;
        this.id = id;
        this.input = input;
    }

    public WriteRequest(String path, int offset, byte[] input) {
        this.path = path;
        this.offset = offset;
        this.input = input;
        this.id = new SecureRandom().nextLong();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof WriteRequest r)) {
            return false;
        }

        return r.getPath().equals(path)
                && r.getOffset() == offset
                && Arrays.equals(r.input, input)
                && r.getId() == id;
    }

    @Override
    public int hashCode() {
        return path.hashCode() + offset + Arrays.hashCode(input);
    }

    public String getPath() {
        return path;
    }

    public byte[] getInput() {
        return input;
    }

    public long getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }

    public void print() {
        System.out.println("Type: Write Request");
        System.out.println("Path: " + path);
        System.out.println("Offset: " + offset);
        System.out.println("Input: " + Arrays.toString(input));
        System.out.println("ID: " + id);
        System.out.println();
    }
}
