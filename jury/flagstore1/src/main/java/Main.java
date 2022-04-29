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
        if (args[0].equals("put")) {
            if (args.length != 3) {
                return "Expected exactly 3 arguments";
            }
            put(args[1], args[2]);
            return "ok";
        } else if (args[0].equals("get")) {
            if (args.length != 2) {
                return "Expected exactly 2 arguments";
            }
            return get(args[1]);
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

    private static void put(String key, String flag) throws IOException {
        write("flag", flag);
        write("key", key);
    }

    private static String get(String key) throws IOException {
        String stored_key = read("key");
        if (key.equals(stored_key)) {
            return String.valueOf(read("flag").hashCode());
        }
        return "Invalid key";
    }
}
