package client;

import server.Reply;
import server.Storage;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Marshalling {

    public static byte[] getBytes(int x) {
        ByteBuffer b = ByteBuffer.allocate(4);
        return b.putInt(x).array();
    }

    public static byte[] getBytes(long x) {
        ByteBuffer b = ByteBuffer.allocate(8);
        return b.putLong(x).array();
    }

    public static byte[] serialize(ReadRequest req) {
        if (req == null) return null;
        // code (1B), path length (4B), path (variable), offset (4B), length (4B), id (8B)

        byte[] pathBytes = req.getPath().getBytes();

        byte[] pathLengthBytes = getBytes(pathBytes.length);
        byte[] offsetBytes = getBytes(req.getOffset());
        byte[] lengthBytes = getBytes(req.getLength());
        byte[] idBytes = getBytes(req.getId());

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
        System.arraycopy(idBytes, 0, output, 13 + pathBytes.length, 8);

        return output;
    }

    public static byte[] serialize(WriteRequest req) {
        if (req == null) return null;
        // code (1B), path length (4B), path (variable)
        // input length (4B), input (variable),
        // offset (4B), id (8B)

        byte[] pathBytes = req.getPath().getBytes();

        byte[] pathLengthBytes = getBytes(pathBytes.length);
        byte[] offsetBytes = getBytes(req.getOffset());
        byte[] inputLengthBytes = getBytes(req.getInput().length);
        byte[] idBytes = getBytes(req.getId());

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
        System.arraycopy(idBytes, 0, output, 13 + pathBytes.length + req.getInput().length, 8);

        return output;
    }

    public static byte[] serialize(PropertiesRequest req) {
        if (req == null) return null;
        // code (1B), path length (4B), path (variable), id (8B)
        byte[] pathBytes = req.getPath().getBytes();
        byte[] pathLengthBytes = getBytes(pathBytes.length);
        byte[] idBytes = getBytes(req.getId());
        byte[] output = new byte[pathBytes.length + pathLengthBytes.length + idBytes.length + 1];
        output[0] = PropertiesRequest.code;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes,0,output,5,pathBytes.length);
        System.arraycopy(idBytes,0,output,5 + pathBytes.length, 8);

        return output;
    }

    public static byte[] serialize(RegisterRequest req) {
        if (req == null) return null;
        // code (1B), path length (4B), path (variable), interval (4b), id (8B)
        byte[] pathBytes = req.getPath().getBytes();
        byte[] pathLengthBytes = getBytes(pathBytes.length);
        byte[] intervalBytes = getBytes(req.getMonitorInterval());
        byte[] idBytes = getBytes(req.getId());
        byte[] output = new byte[pathBytes.length +
                pathLengthBytes.length +
                intervalBytes.length +
                idBytes.length + 1];
        output[0] = RegisterRequest.code;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes, 0, output, 5, pathBytes.length);
        System.arraycopy(intervalBytes, 0, output, 5 + pathBytes.length, 4);
        System.arraycopy(idBytes, 0, output, 9 + pathBytes.length, 8);

        return output;
    }

    public static byte[] serialize(Reply reply) {
        if (reply == null) return null;

        // code (1B), result (1B), ID (8B), body (variable)

        byte[] output = new byte[reply.getBody().length + 10];
        output[0] = Reply.code;
        output[1] = reply.getResult();
        byte[] idBytes = getBytes(reply.getId());
        System.arraycopy(idBytes, 0, output, 2, 8);
        System.arraycopy(reply.getBody(), 0, output, 10, reply.getBody().length);

        return output;
    }

    public static Object deserialize(byte[] bytes) {
        if (bytes == null) return null;
        if (bytes.length == 0) {
            System.out.println("Error: deserialize input empty");
            return null;
        }

        byte code = bytes[0];

        if (code == ReadRequest.code) {
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);

            int offset = ByteBuffer.wrap(bytes, 5 + pathLength, 4).getInt();
            int length = ByteBuffer.wrap(bytes, 9 + pathLength, 4).getInt();
            long id = ByteBuffer.wrap(bytes, 13 + pathLength, 8).getLong();

            String path = new String(pathBytes);
            return new ReadRequest(path, offset, length, id);

        } else if (code == WriteRequest.code) {
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);
            int inputLength = ByteBuffer.wrap(bytes, 5 + pathLength, 4).getInt();
            byte[] inputBytes = Arrays.copyOfRange(bytes, 9 + pathLength, 9 + pathLength + inputLength);
            int offset = ByteBuffer.wrap(bytes, 9 + pathLength + inputLength, 4).getInt();
            long id = ByteBuffer.wrap(bytes, 13 + pathLength + inputLength, 8).getLong();

            String path = new String(pathBytes);
            return new WriteRequest(path, offset, inputBytes, id);

        } else if (code == Reply.code) {
            byte result = bytes[1];
            long id = ByteBuffer.wrap(bytes, 2, 8).getLong();
            byte[] body = Arrays.copyOfRange(bytes, 10, bytes.length);

            return new Reply(result, id, body);
        } else if (code == PropertiesRequest.code) {
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);
            long id = ByteBuffer.wrap(bytes, 5 + pathLength, 8).getLong();

            String path = new String(pathBytes);
            return new PropertiesRequest(path, id);
        } else if (code == RegisterRequest.code) {
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);
            int interval = ByteBuffer.wrap(bytes, 5 + pathLength, 4).getInt();
            long id = ByteBuffer.wrap(bytes, 9 + pathLength, 8).getLong();
            String path = new String(pathBytes);
            return new RegisterRequest(path, interval, id);
        } else {
            System.out.println("Error: request header invalid");
            return null;
        }
    }

    public static void main(String[] args) {
        String filePath = "test.txt";
        ReadRequest readRequest = new ReadRequest(filePath, 5, 30);

        System.out.println("Testing Read Request");
        System.out.println();
        System.out.println("Original");
        readRequest.print();
        System.out.println("Reconstructed");
        ReadRequest readCopy = (ReadRequest) Marshalling.deserialize(Marshalling.serialize(readRequest));
        assert readCopy != null;
        readCopy.print();
        System.out.println("Both requests equal: " + readRequest.equals(readCopy));
        System.out.println();

        Storage store = new Storage();
        store.populateStorage(filePath,"0123456789");
        Reply reply = store.readBytes(readRequest);

        System.out.println("Testing Reply");
        System.out.println();
        System.out.println("Original");
        reply.print();
        System.out.println("Reconstructed");
        Reply replyCopy = (Reply) Marshalling.deserialize(Marshalling.serialize(reply));
        assert replyCopy != null;
        replyCopy.print();

        WriteRequest writeRequest = new WriteRequest(filePath, 1,
                ByteBuffer.allocate(4).putInt(999).array().clone());
        System.out.println("Testing Write Request");
        System.out.println();
        System.out.println("Original");
        writeRequest.print();
        System.out.println("Reconstructed");
        WriteRequest writeCopy = (WriteRequest) Marshalling.deserialize(Marshalling.serialize(writeRequest));
        assert writeCopy != null;
        writeCopy.print();

        PropertiesRequest attributeRequest = new PropertiesRequest(filePath);
        System.out.println("Testing Attribute Request");
        System.out.println();
        System.out.println("Original");
        attributeRequest.print();
        System.out.println("Reconstructed");
        PropertiesRequest attributeCopy = (PropertiesRequest) Marshalling.deserialize(Marshalling.serialize(attributeRequest));
        assert attributeCopy != null;
        attributeCopy.print();

        RegisterRequest registerRequest = new RegisterRequest(filePath, 3);
        System.out.println("Testing Register Request");
        System.out.println();
        System.out.println("Original");
        registerRequest.print();
        System.out.println("Reconstructed");
        RegisterRequest registerCopy = (RegisterRequest) Marshalling.deserialize(Marshalling.serialize(registerRequest));
        assert registerCopy != null;
        registerCopy.print();
    }
}


