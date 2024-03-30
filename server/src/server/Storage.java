package server;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import client.*;

import static java.lang.Math.min;
import static java.util.Map.entry;

public class Storage {
    private static byte b(int i) {
        return (byte) i;
    }
    private Path dirPath;
    // 0 - Read request success
    // 1 - Write request success

    // 10 - File does not exist
    // 11 - Offset exceeds file length
    // 12 - Offset less than zero
    public static final Map<Byte, String> resultMap = Map.ofEntries(
            entry(b(0), "Read request success"),
            entry(b(1), "Write request success"),
            entry(b(10), "File does not exist"),
            entry(b(11), "Offset exceeds file length"),
            entry(b(12), "Offset less than 0"),
            entry(b(20), "Null reply")
    );

    public Storage() {
        dirPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toPath();
        dirPath = dirPath.resolve("CZ4013 Storage");
        File dir = dirPath.toFile();
        if (dir.mkdirs()) {
            System.out.println("New directory created at " + dirPath);
        } else {
            System.out.println("Existing directory found at " + dirPath);
        }
    }

    public Reply readBytes(ReadRequest req) {

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

            return new Reply((byte) 0, req.getId(), output);
        } catch (Exception e) {
            System.out.println("Error: file read error");
            return new Reply((byte) 10, req.getId());
        }
    }

    public Reply writeBytes(WriteRequest write) {
        Path p = dirPath.resolve(write.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return new Reply((byte) 10, write.getId());
        } else if (write.getOffset() < 0) {
            return new Reply((byte) 12, write.getId());
        }

        if (write.getOffset() >= f.length()) {
            return new Reply((byte) 11, write.getId());
        }

        try {
            byte[] bytes = Files.readAllBytes(p);
            byte[] output = new byte[bytes.length + write.getInput().length];

            System.arraycopy(bytes, 0, output, 0, write.getOffset());
            System.arraycopy(write.getInput(), 0, output, write.getOffset(), write.getInput().length);
            System.arraycopy(bytes, write.getOffset(), output,
                    write.getOffset() + write.getInput().length, bytes.length - write.getOffset());

            Files.write(p, output);
            return new Reply((byte) 1, write.getId());
        } catch (Exception e) {
            System.out.println("Error: file read error");
            return new Reply((byte) 10, write.getId());
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
    }
}
