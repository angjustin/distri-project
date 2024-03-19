import java.nio.ByteBuffer;
import java.util.Arrays;

public class Marshalling {
    public static byte[] serialize(ReadRequest req) {
        byte[] pathBytes = req.getPath().getBytes();

        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] pathLengthBytes = b.putInt(0, pathBytes.length).array().clone();
        byte[] offsetBytes = b.putInt(0, req.getOffset()).array().clone();
        byte[] lengthBytes = b.putInt(0, req.getLength()).array().clone();

        byte[] output = new byte[pathBytes.length + 14];

        // 0 - ReadRequest, 1 - WriteRequest, 2 - MonitorRequest
        output[0] = 0;
        System.arraycopy(pathLengthBytes, 0, output, 1, 4);
        System.arraycopy(pathBytes, 0, output, 5, pathBytes.length);
        System.arraycopy(offsetBytes, 0, output, 5 + pathBytes.length, 4);
        System.arraycopy(lengthBytes, 0, output, 9 + pathBytes.length, 4);
        output[output.length - 1] = req.getId();

        return output;
    }

    public static Object deserialize(byte[] bytes) {
        if (bytes.length == 0) {
            System.out.println("Deserialize input null!");
            return null;
        }

        if (bytes[0] == 0) {
            // ReadRequest
            int pathLength = ByteBuffer.wrap(bytes, 1, 4).getInt();
            byte[] pathBytes = Arrays.copyOfRange(bytes, 5, 5 + pathLength);
            String path = new String(pathBytes);
            int offset = ByteBuffer.wrap(bytes, 5 + pathLength, 4).getInt();
            int length = ByteBuffer.wrap(bytes, 9 + pathLength, 4).getInt();
            byte id = bytes[bytes.length - 1];

            return new ReadRequest(path, offset, length, id);

        } else {
            System.out.println("Request header invalid!");
            return null;
        }
    }

    public static void main(String[] args) {
        ReadRequest r = new ReadRequest("/foo", 2, 3);
        System.out.println("Testing ReadRequest");
        System.out.println();
        System.out.println("Original");
        r.print();
        System.out.println();
        System.out.println("Reconstructed");
        ReadRequest rCopy = (ReadRequest) Marshalling.deserialize(Marshalling.serialize(r));
        assert rCopy != null;
        rCopy.print();
    }
}


