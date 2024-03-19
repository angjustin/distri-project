package client;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Marshalling {
    public static byte[] serialize(ReadRequest req) {

        // code (1B), path length (4B), path (variable), offset (4B), length (4B), id (1B)

        byte[] pathBytes = req.getPath().getBytes();

        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] pathLengthBytes = b.putInt(0, pathBytes.length).array().clone();
        byte[] offsetBytes = b.putInt(0, req.getOffset()).array().clone();
        byte[] lengthBytes = b.putInt(0, req.getLength()).array().clone();

        byte[] output = new byte[pathBytes.length + 14];

        output[0] = ReadRequest.code;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes, 0, output, 5, pathBytes.length);
        System.arraycopy(offsetBytes, 0, output, 5 + pathBytes.length, 4);
        System.arraycopy(lengthBytes, 0, output, 9 + pathBytes.length, 4);
        output[output.length - 1] = req.getId();

        return output;
    }

    public static byte[] serialize(Reply reply) {
        // code (1B), result (1B), serialized request (variable)
        byte[] requestBytes = Marshalling.serialize(reply.getRequest());
        byte[] output = new byte[requestBytes.length + 2];
        output[0] = Reply.code;
        output[1] = reply.getResult();
        System.arraycopy(requestBytes, 0, output, 2, requestBytes.length);
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
            byte id = bytes[bytes.length - 1];

            return new ReadRequest(path, offset, length, id);

        } else if (code == Reply.code) {
            byte[] requestBytes = new byte[bytes.length - 2];
            System.arraycopy(bytes, 2, requestBytes, 0, requestBytes.length);
            Object request = Marshalling.deserialize(requestBytes);
            return new Reply(request, bytes[1]);
        }
        else {
            System.out.println("Error: request header invalid");
            return null;
        }
    }

    public static void main(String[] args) {
        ReadRequest request = new ReadRequest("/foo", 2, 3);
        System.out.println("Testing ReadRequest");
        System.out.println();
        System.out.println("Original");
        request.print();
        System.out.println("Reconstructed");
        ReadRequest requestCopy = (ReadRequest) Marshalling.deserialize(Marshalling.serialize(request));
        assert requestCopy != null;

        requestCopy.print();
        System.out.println("Both requests equal: " + request.equals(requestCopy));
        System.out.println();

        Reply reply = new Reply(request, (byte) 1);

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


