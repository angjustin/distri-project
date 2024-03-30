package server;

import java.util.Arrays;

public final class Reply {
    public static final byte code = 0;
    private final byte result;
    private final byte[] body;
    private final long id;

    public long getId() {
        return id;
    }

    public byte getResult() {
        return result;
    }

    public byte[] getBody() {
        return body;
    }

    public Reply(byte result, long id, byte[] body) {
        this.result = result;
        this.id = id;
        this.body = body;
    }
    public Reply(byte result, long id) {
        this.result = result;
        this.id = id;
        this.body = new byte[0];
    }


    public void print() {
        System.out.println("Type: Reply");
        System.out.println("Result: " + result);
        System.out.println("Body: " + Arrays.toString(body));
        System.out.println("ID: " + id);
        System.out.println();
    }
}
