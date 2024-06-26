package client;

import java.security.SecureRandom;

public final class ReadRequest {
    public static final byte code = 1;
    private final String path;
    private final int offset;
    private final int length;
    private final long id;
    public ReadRequest(String path, int offset, int length) {
        this.path = path;
        this.offset = offset;
        this.length = length;
        this.id = new SecureRandom().nextLong();
    }

    public ReadRequest(String path, int offset, int length, long id) {
        this.path = path;
        this.offset = offset;
        this.length = length;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ReadRequest r)) {
            return false;
        }

        return r.getPath().equals(path)
                && r.getOffset() == offset
                && r.getLength() == length
                && r.getId() == id;
    }

    @Override
    public int hashCode() {
        return path.hashCode() + offset + length;
    }

    public void print() {
        System.out.println("Type: Read Request");
        System.out.println("Path: " + path);
        System.out.println("Offset: " + offset);
        System.out.println("Length: " + length);
        System.out.println("ID: " + id);
        System.out.println();
    }
}
