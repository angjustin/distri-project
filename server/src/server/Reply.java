package server;

import client.*;

import java.security.SecureRandom;
import java.util.Arrays;

import static server.Storage.resultMap;

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

    public Reply() {
        this.result = 20;
        this.id = new SecureRandom().nextLong();
        this.body = new byte[0];
    }


    public void print() {
        System.out.println("Type: Reply");
        System.out.println("Result: " + result);
        System.out.println("Body: " + Arrays.toString(body));
        System.out.println("ID: " + id);
        System.out.println();
    }

    public void printClient() {
        System.out.println();
        System.out.println("Response: " + resultMap.get(result));
        if (result == ReadRequest.code | result == 7) {  // read request success or notify of update
            System.out.println("---Read output---");
            System.out.println(new String(body));
            System.out.println();
        } else if (result == PropertiesRequest.code) {
            Cache.Record record = (Cache.Record) Marshalling.deserialize(body);
            record.print();
            System.out.println();
        } else {
            System.out.println();
        }
    }
}
