package lebedev.cloudydays.api;

import lebedev.cloudydays.SealedStorageInit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Storage {
    SealedStorageInit init;

    public Storage(SealedStorageInit init) {
        this.init = init;
    }

    public File openFile(String name) {
        new File(this.init.getBasePath()).mkdirs();
        return new File(this.init.getBasePath() + name);
    }

    public byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    public void writeFile(File file, byte[] data) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }
}
