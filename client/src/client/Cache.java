package client;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static java.lang.Math.min;
import static server.Storage.humanReadableByteCountSI;

public class Cache {
    private final HashMap<String, Record> records;
    private final HashMap<String, byte[]> files;
    private final int freshness;

    // Cache record
    public final static class Record {
        public static final byte code = 5;
        private final long localValidMillis;
        private final long serverValidMillis;
        private final long creationMillis;
        private final long size;

        public Record (long localTime, long serverTime, long creationTime, long size) {
            this.localValidMillis = localTime;
            this.serverValidMillis = serverTime;
            this.creationMillis = creationTime;
            this.size = size;
        }

        public Record() {
            this.localValidMillis = System.currentTimeMillis();
            this.serverValidMillis = System.currentTimeMillis();
            this.creationMillis = System.currentTimeMillis();
            this.size = 0;
        }

        public void print() {
            // print cache record
            DateTimeFormatter dt = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault());
            Instant creation = Instant.ofEpochMilli(this.creationMillis);
            Instant modify = Instant.ofEpochMilli(this.serverValidMillis);

            String sb = "---Properties---\n" +
                    padLeft("Last modified time: ", 20) + dt.format(modify) + "\n" +
                    padLeft("Creation time: ", 20) + dt.format(creation) + "\n" +
                    padLeft("Size: ",20) + humanReadableByteCountSI(this.size) + "\n";
            System.out.print(sb);
        }

        public static String padRight(String s, int n) {
            return String.format("%-" + n + "s", s);
        }

        public static String padLeft(String s, int n) {
            return String.format("%" + n + "s", s);
        }

        public long getCreationMillis() {
            return creationMillis;
        }

        public long getLocalValidMillis() {
            return localValidMillis;
        }

        public long getServerValidMillis() {
            return serverValidMillis;
        }

        public long getSize() {
            return size;
        }
    }

    public Cache(int freshness) {
        this.records = new HashMap<>();
        this.files = new HashMap<>();
        this.freshness = freshness;
    }

    public boolean hasRecord(String path) {
        return this.records.containsKey(path);
    }

    // check if a file's record is older than the freshness interval
    public boolean isRecordStale(String path) {
        // check if cache entry is still fresh
        if (!hasRecord(path)) return true;

        Record record = records.get(path);
        return System.currentTimeMillis() - record.localValidMillis > freshness;
    }

    // check if the record of a given file has the same last modified time as the given record
    public boolean isRecordValid(String path, Record serverRecord) {
        if (!hasRecord(path)) return false;
        return records.get(path).getServerValidMillis() == serverRecord.getServerValidMillis();
    }

    // replaces an old record with a new one of the same file path
    public void refreshRecord(String path, Record record) {
        if (!hasRecord(path)) return;
        records.put(path, record);
    }

    // read bytes from a cached file
    public byte[] getBytes(ReadRequest request) {
        byte[] bytes = this.files.get(request.getPath());
        if (bytes == null) {
            System.out.println("Error, File does not exist");
            return null;
        }

        if (request.getOffset() >= bytes.length) {
            System.out.println("Error, Offset exceeds file length");
            return null;
        } else if (request.getOffset() < 0) {
            System.out.println("Error, Offset less than 0");
            return null;
        }

        if (request.getLength() < 0) {
            System.out.println("Error, Number of bytes to read less than 0");
            return null;
        }

        int length = min(request.getLength(), bytes.length - request.getOffset());
        byte[] output = new byte[length];

        System.arraycopy(bytes, request.getOffset(), output, 0, length);
        return output;
    }

    public void printFile(ReadRequest request) {
        byte[] bytes = getBytes(request);
        System.out.println();
        if (bytes != null) {
            System.out.println(new String(bytes));
            System.out.println();
        }
    }

    public void addFile(String path, Record record, byte[] bytes) {
        this.records.put(path, record);
        this.files.put(path, bytes);
    }

    public void deleteFile(String path) {
        this.records.remove(path);
        this.files.remove(path);
    }
}
