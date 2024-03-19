package client;

import java.util.Arrays;
import java.util.Random;

public final class WriteRequest {
    public static final byte code = 2;
    private final String path;
    private final int offset;
    private final byte[] input;
    private final int id;

    public WriteRequest(String path, int offset, int id, byte[] input) {
        this.path = path;
        this.offset = offset;
        this.id = id;
        this.input = input;
    }

    public WriteRequest(String path, int offset, byte[] input) {
        this.path = path;
        this.offset = offset;
        this.input = input;
        this.id = new Random().nextInt();
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

    public int getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }
}
