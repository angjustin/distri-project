package client;

public final class Reply {
    public static final byte code = 0;
    private final Object request;

    // 0 - Success
    // 1 - File does not exist
    // 2 - Offset exceeds file length
    private final byte result;
    public ReadRequest getRequest() {
        if (request instanceof ReadRequest) {
            return (ReadRequest) request;
        } else {
            return null;
        }
    }

    public byte getResult() {
        return result;
    }

    public Reply(Object request, byte result) {
        this.request = request;
        this.result = result;
    }

    public void print() {
        System.out.println("Type: Reply");
        System.out.println("Result: " + result);
        System.out.println("Request: {");
        System.out.println();
        assert getRequest() != null;
        getRequest().print();
        System.out.println("}");
        System.out.println();
    }
}
