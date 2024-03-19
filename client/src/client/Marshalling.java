package client;

import server.Storage;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Marshalling {
    public static byte[] serialize(ReadRequest req) {

        // code (1B), path length (4B), path (variable), offset (4B), length (4B), id (4B)

        byte[] pathBytes = req.getPath().getBytes();

        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] pathLengthBytes = b.putInt(0, pathBytes.length).array().clone();
        byte[] offsetBytes = b.putInt(0, req.getOffset()).array().clone();
        byte[] lengthBytes = b.putInt(0, req.getLength()).array().clone();
        byte[] idBytes = b.putInt(0, req.getId()).array().clone();

        byte[] output = new byte[pathBytes.length
                + pathLengthBytes.length
                + offsetBytes.length
                + lengthBytes.length
                + idBytes.length
                + 1];

        output[0] = ReadRequest.code;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes, 0, output, 5, pathBytes.length);
        System.arraycopy(offsetBytes, 0, output, 5 + pathBytes.length, 4);
        System.arraycopy(lengthBytes, 0, output, 9 + pathBytes.length, 4);
        System.arraycopy(idBytes, 0, output, 13 + pathBytes.length, 4);

        return output;
    }

    public static byte[] serialize(WriteRequest req) {
        // code (1B), path length (4B), path (variable),
        // input length (4B), input (variable),
        // offset (4B), id (4B)

        byte[] pathBytes = req.getPath().getBytes();

        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] pathLengthBytes = b.putInt(0, pathBytes.length).array().clone();
        byte[] offsetBytes = b.putInt(0, req.getOffset()).array().clone();
        byte[] inputLengthBytes = b.putInt(0, req.getInput().length).array().clone();
        byte[] idBytes = b.putInt(0, req.getId()).array().clone();

        byte[] output = new byte[pathBytes.length
                + pathLengthBytes.length
                + inputLengthBytes.length
                + req.getInput().length
                + offsetBytes.length
                + idBytes.length
                + 1];

        output[0] = WriteRequest.code;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes, 0, output, 5, pathBytes.length);
        System.arraycopy(inputLengthBytes, 0, output, 5 + pathBytes.length, 4);
        System.arraycopy(req.getInput(), 0, output, 9 + pathBytes.length, req.getInput().length);
        System.arraycopy(offsetBytes, 0, output, 9 + pathBytes.length + req.getInput().length, 4);
        System.arraycopy(idBytes, 0, output, 13 + pathBytes.length + req.getInput().length, 4);

        return output;
    }

    public static byte[] serialize(Reply reply) {
        // code (1B), result (1B), ID (4B), body (variable)

        byte[] output = new byte[reply.getBody().length + 6];
        output[0] = Reply.code;
        output[1] = reply.getResult();
        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] idBytes = b.putInt(0, reply.getId()).array().clone();
        System.arraycopy(idBytes, 0, output, 2, 4);
        System.arraycopy(reply.getBody(), 0, output, 6, reply.getBody().length);

        return output;
    }

    public static Object deserialize(byte[] bytes) {
        if (bytes.length == 0) {
            System.out.println("Error: deserialize input null");
            return null;
        }

        byte code = bytes[0];

        if (code == ReadRequest.code) {
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);
            String path = new String(pathBytes);
            int offset = ByteBuffer.wrap(bytes, 5 + pathLength, 4).getInt();
            int length = ByteBuffer.wrap(bytes, 9 + pathLength, 4).getInt();
            int id = ByteBuffer.wrap(bytes, 13 + pathLength, 4).getInt();

            return new ReadRequest(path, offset, length, id);

        } else if (code == Reply.code) {
            byte result = bytes[1];
            int id = ByteBuffer.wrap(bytes, 2, 4).getInt();
            byte[] body = Arrays.copyOfRange(bytes, 6, bytes.length);

            return new Reply(result, id, body);
        }
        else {
            System.out.println("Error: request header invalid");
            return null;
        }
    }

    public static void main(String[] args) {
        String filePath = "test.txt";
        ReadRequest request = new ReadRequest(filePath, 2, 3);
        System.out.println("Testing Read Request");
        System.out.println();
        System.out.println("Original");
        request.print();
        System.out.println("Reconstructed");
        ReadRequest requestCopy = (ReadRequest) Marshalling.deserialize(Marshalling.serialize(request));
        assert requestCopy != null;

        requestCopy.print();
        System.out.println("Both requests equal: " + request.equals(requestCopy));
        System.out.println();

        Storage store = new Storage();
        store.populateStorage(filePath,"0123456789");
        Reply reply = store.readBytes(request);

        System.out.println("Testing Reply");
        System.out.println();
        System.out.println("Original");
        reply.print();

        System.out.println("Reconstructed");
        Reply replyCopy = (Reply) Marshalling.deserialize(Marshalling.serialize(reply));
        assert replyCopy != null;
        replyCopy.print();
    }
}


