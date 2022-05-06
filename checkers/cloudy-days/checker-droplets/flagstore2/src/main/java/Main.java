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

    private static String select(String[] args) throws IOException, Crypto.WrongNonceSizeException, Crypto.WrongKeySizeException {
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

    private static void writeBytes(String name, byte[] value) throws IOException {
        File file = storage.openFile(name);
        storage.writeFile(file, value);
    }

    private static byte[] readBytes(String name) throws IOException {
        File file = storage.openFile(name);
        return storage.readFile(file);
    }

    private static void randomBytes(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte)(Math.random() * 256);
        }
    }

    private static void memcpy(byte[] dst, int dstOffset, byte[] src, int srcOffset, int length) {
        for (int i = 0; i < length && i < dst.length - dstOffset && i < src.length - srcOffset; i++) {
            dst[dstOffset + i] = src[srcOffset + i];
        }
    }

    private static void put(String key, String flag) throws IOException, Crypto.WrongNonceSizeException, Crypto.WrongKeySizeException {
        byte[] nonce = new byte[12];
        randomBytes(nonce);
        writeBytes("nonce", nonce);
        byte[] keyBytes = new byte[32];
        memcpy(keyBytes, 0, key.getBytes(StandardCharsets.UTF_8), 0, 32);
        Crypto crypto = new Crypto(keyBytes, nonce, 0);
        byte[] flagBytes = flag.getBytes(StandardCharsets.UTF_8);
        crypto.encrypt(flagBytes, flagBytes, flagBytes.length);
        writeBytes("flag", flagBytes);
    }

    private static String get(String key) throws IOException, Crypto.WrongNonceSizeException, Crypto.WrongKeySizeException {
        byte[] nonce = readBytes("nonce");
        byte[] flagBytes = readBytes("flag");
        byte[] keyBytes = new byte[32];
        memcpy(keyBytes, 0, key.getBytes(StandardCharsets.UTF_8), 0, 32);
        Crypto crypto = new Crypto(keyBytes, nonce, 0);
        crypto.decrypt(flagBytes, flagBytes, flagBytes.length);
        return String.valueOf(new String(flagBytes).hashCode());
    }
}
