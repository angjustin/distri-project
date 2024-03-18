public class ReadRequest {
    public String path;
    public int offset;
    public int length;

    public ReadRequest() {}

    public ReadRequest(String path, int offset, int length) {
        this.path = path;
        this.offset = offset;
        this.length = length;
    }

    public void print() {
        System.out.println("Read Request");
        System.out.println("Path: " + path);
        System.out.println("Offset: " + offset);
        System.out.println("Length: " + length);
    }
}
