package server;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

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
            FileWriter writer = new FileWriter(temp);
            writer.write(s);
            writer.close();
            System.out.println("Wrote " + s + " to " + temp.getName());
        } catch (Exception e) {
            System.out.println("Error: writing to " + temp.getName() + " failed");
        }
    }

    public static void main(String[] args) {
        Storage store = new Storage();
        store.populateStorage("0123456789");
    }
}
