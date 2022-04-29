package lebedev.cloudydays;

import java.io.File;

public class SealedStorageInit {
    String basePath;

    public SealedStorageInit(String baseDirectory, String dropletName) {
        basePath = baseDirectory + File.separator + dropletName + File.separator;
    }

    public String getBasePath() {
        return basePath;
    }
}
