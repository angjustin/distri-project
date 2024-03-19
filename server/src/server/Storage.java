package server;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import client.*;

import static java.lang.Math.min;

public class Storage {
    private Path dirPath;
    private File dir;
    public Storage() {
        dirPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toPath();
        dirPath = dirPath.resolve("CZ4013 Storage");
        dir = dirPath.toFile();
        if (dir.mkdirs()) {
            System.out.println("New directory created at " + dirPath);
        } else {
            System.out.println("Existing directory found at " + dirPath);
        }
    }

    public byte[] readBytes(ReadRequest req) {

        Path p = dirPath.resolve(req.getPath());
        File f = p.toFile();
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: invalid path " + p);
            return null;
        }

        if (req.getOffset() >= f.length()) {
            System.out.println("Error: offset exceeds file length");
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(p);

            int length = min(req.getLength(), bytes.length - req.getOffset());
            byte[] output = new byte[length];

            System.arraycopy(bytes, req.getOffset(), output, 0, length);

            return output;
        } catch (Exception e) {
            System.out.println("Error: file read error");
            return null;
        }

    }

    public void populateStorage(String s) {
        File temp = dirPath.resolve("test.txt").toFile();
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
        } catch (Exception e) {
            System.out.println("Error: writing to " + temp.getName() + " failed");
        }
    }

    public static void main(String[] args) {
        Storage store = new Storage();
        store.populateStorage("0123456789");
        ReadRequest r = new ReadRequest("test.txt", 2, 3);
        System.out.println(Arrays.toString(store.readBytes(r)));
        System.out.println(new String(store.readBytes(r)));
    }
}
