package client;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static server.Storage.humanReadableByteCountSI;

public class Cache {
    private HashMap<String, Record> map;

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

    public void addRecord(String path, Record record) {
        map.put(path, record);
    }

    public Record getRecord(String path) {
        return map.get(path);
    }
}
