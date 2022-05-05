import lebedev.cloudydays.api.Storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    private static Storage storage;

    public static String main(String[] args, Storage storage) {
        Main.storage = storage;

        if (args.length == 0) {
            return "Expected at least 1 argument";
        }

        try {
            return select(args);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String select(String[] args) throws IOException {
        if (args[0].equals("save")) {
            if (args.length != 2) {
                return "Expected exactly 2 arguments";
            }
            String oldData = "<no data>";
            try {
                oldData = load();
            } catch (Exception ignored) {
            }
            save(args[1]);
            return oldData;
        }
        return "Unknown command ".concat(args[0]);
    }

    private static void write(String name, String value) throws IOException {
        File file = storage.openFile(name);
        storage.writeFile(file, value.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(String name) throws IOException {
        File file = storage.openFile(name);
        return new String(storage.readFile(file));
    }

    private static void save(String data) throws IOException {
        write("data", data);
    }

    private static String load() throws IOException {
        return read("data");
    }
}
