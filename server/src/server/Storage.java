package server;

import javax.swing.*;
import java.io.File;
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
            System.out.println();
        } else {
            System.out.println("Existing directory found at " + dirPath);
        }
    }

    public static void main(String[] args) {
        Storage store = new Storage();
    }
}
