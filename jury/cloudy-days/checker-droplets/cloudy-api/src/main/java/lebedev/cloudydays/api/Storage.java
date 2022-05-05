package lebedev.cloudydays.api;

import java.io.File;
import java.io.IOException;

public abstract class Storage {
    public abstract File openFile(String name);

    public abstract byte[] readFile(File file) throws IOException;

    public abstract void writeFile(File file, byte[] data) throws IOException;
}
