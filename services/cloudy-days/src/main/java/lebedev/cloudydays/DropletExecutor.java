package lebedev.cloudydays;

import lebedev.cloudydays.api.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;

public class DropletExecutor {
    private final String jarDirectory;
    private final String storageDirectory;

    private final Logger logger = LoggerFactory.getLogger(DropletExecutor.class);

    public DropletExecutor(String jarDirectory, String storageDirectory) {
        this.jarDirectory = jarDirectory;
        this.storageDirectory = storageDirectory;
    }

    public String execute(String dropletName, String[] parameters) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        URL jarUrl = new URL(new File(jarDirectory).toURI().toURL() + dropletName + ".jar");
        logger.info("Executing " + dropletName + " located at " + jarUrl + " with parameters " + Arrays.toString(parameters));
        SecureClassLoader loader = new SecureClassLoader(jarUrl, Droplet.class.getClassLoader());
        Class<?> clazz = loader.loadClass("Main");
        Method main = clazz.getDeclaredMethod("main", String[].class, Storage.class);
        return (String) main.invoke(null, parameters, new Storage(new SealedStorageInit(storageDirectory, dropletName)));
    }
}
