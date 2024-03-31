package server;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;

import client.*;

import static java.lang.Math.min;
import static java.util.Map.entry;

public class Storage {
    private static byte b(int i) {
        return (byte) i;
    }
    private Path dirPath;

    public static final Map<Byte, String> resultMap = Map.ofEntries(
            entry(b(1), "Read request success"),
            entry(b(2), "Write request success"),
            entry(b(4), "Properties request success"),
            entry(b(6), "File request success"),
            entry(b(10), "File does not exist"),
            entry(b(11), "Offset exceeds file length"),
            entry(b(12), "Offset less than 0"),
            entry(b(20), "Null reply")
    );

    public Storage() {
        this("CZ4013 Storage");
    }

    public Storage(String path) {
        dirPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toPath();
        dirPath = dirPath.resolve(path);
        File dir = dirPath.toFile();
        if (dir.mkdirs()) {
            System.out.println("New directory created at " + dirPath);
        } else {
            System.out.println("Existing directory found at " + dirPath);
        }
    }

    public Reply getFile(FileRequest req) {
        Path p = dirPath.resolve(req.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return new Reply((byte) 10, req.getId());
        }

        try {
            byte[] output = Files.readAllBytes(p);

            return new Reply(FileRequest.code, req.getId(), output);
        } catch (Exception e) {
            return new Reply((byte) 10, req.getId());
        }

    }

    public Reply readBytes(ReadRequest req) {   // no longer used, files are cached first then read locally

        Path p = dirPath.resolve(req.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return new Reply((byte) 10, req.getId());
        }

        if (req.getOffset() >= f.length()) {
            return new Reply((byte) 11, req.getId());
        } else if (req.getOffset() < 0) {
            return new Reply((byte) 12, req.getId());
        }

        try {
            byte[] bytes = Files.readAllBytes(p);

            int length = min(req.getLength(), bytes.length - req.getOffset());
            byte[] output = new byte[length];

            System.arraycopy(bytes, req.getOffset(), output, 0, length);

            return new Reply(ReadRequest.code, req.getId(), output);
        } catch (Exception e) {
            return new Reply((byte) 10, req.getId());
        }
    }

    public Reply writeBytes(WriteRequest write) {
        Path p = dirPath.resolve(write.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return new Reply((byte) 10, write.getId());
        }

        if (write.getOffset() >= f.length()) {
            return new Reply((byte) 11, write.getId());
        } else if (write.getOffset() < 0) {
            return new Reply((byte) 12, write.getId());
        }

        try {
            byte[] bytes = Files.readAllBytes(p);
            byte[] output = new byte[bytes.length + write.getInput().length];

            System.arraycopy(bytes, 0, output, 0, write.getOffset());
            System.arraycopy(write.getInput(), 0, output, write.getOffset(), write.getInput().length);
            System.arraycopy(bytes, write.getOffset(), output,
                    write.getOffset() + write.getInput().length, bytes.length - write.getOffset());

            Files.write(p, output);
            return new Reply(WriteRequest.code, write.getId());
        } catch (Exception e) {
            return new Reply((byte) 10, write.getId());
        }
    }

    public Reply getProperties(PropertiesRequest req) {
        Path p = dirPath.resolve(req.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return new Reply((byte) 10, req.getId());
        }

        try {
            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
            Cache.Record record = new Cache.Record(System.currentTimeMillis(),
                    attr.lastModifiedTime().toMillis(),
                    attr.creationTime().toMillis(),
                    attr.size());
            byte[] output = Marshalling.serialize(record);

            return new Reply(PropertiesRequest.code, req.getId(), output);

        } catch (Exception e) {
            e.printStackTrace();
            return new Reply((byte) 10, req.getId());
        }
    }

    public void populateStorage(String path, String s) {
        File temp = dirPath.resolve(path).toFile();
        try {
            if (temp.createNewFile()) {
                System.out.println("Created " + temp.getName());
            } else {
                System.out.println(temp.getName() + " already exists");
            }
        }
         catch (Exception e) {
            System.out.println("Error: creating " + temp.getName() + " failed");
        }

        try {
            Files.write(temp.toPath(), s.getBytes());
            System.out.println("Wrote " + s + " to " + temp.getName());
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error: writing to " + temp.getName() + " failed");
        }
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static void main(String[] args) {
        String filePath = "test.txt";
        Storage store = new Storage();
        store.populateStorage(filePath, "0123456789");
        ReadRequest r = new ReadRequest(filePath, 0, 999);
        System.out.println("Reply from read request");
        store.readBytes(r).print();
        System.out.println(filePath + ": " + new String(store.readBytes(r).getBody()));
        System.out.println();
        System.out.println("Reply from write request");
        WriteRequest w = new WriteRequest(filePath, 1, " hello ".getBytes());
        store.writeBytes(w).print();
        System.out.println("Reply from read request after writing");
        store.readBytes(r).print();
        System.out.println(filePath + ": " + new String(store.readBytes(r).getBody()));
        System.out.println();
        System.out.println("Attribute request");
        PropertiesRequest a = new PropertiesRequest(filePath);
        store.getProperties(a).print();
    }
}
