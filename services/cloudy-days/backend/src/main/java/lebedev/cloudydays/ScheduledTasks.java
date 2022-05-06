package lebedev.cloudydays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

@Component
public class ScheduledTasks {
    private final DropletRepository dropletRepository;
    private final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private File baseJarDirectory;
    private File baseStorageDirectory;

    public ScheduledTasks(DropletRepository dropletRepository) {
        this.dropletRepository = dropletRepository;
    }

    @Scheduled(fixedRate = 15000)
    public void cleanOldDroplets() {
        ArrayList<Droplet> deletionList = new ArrayList<>();
        for (Droplet droplet : dropletRepository.findAll()) {
            long duration = (new Date()).getTime() - droplet.getCreated().getTime();
            if (duration > 15 * 60 * 1000) {
                deletionList.add(droplet);
                logger.info("Deleting old droplet " + droplet.getName() + " (deployed on " + droplet.getCreated() + ")");
                try {
                    File jarFile = new File(baseJarDirectory.getAbsolutePath() + File.separator + droplet.getName() + ".jar");
                    jarFile.delete();
                } catch (Exception e) {
                    logger.error("Jar deletion error", e);
                }
                try {
                    File dataDir = new File(baseStorageDirectory.getAbsolutePath() + File.separator + droplet.getName());
                    dataDir.delete();
                } catch (Exception e) {
                    logger.error("Data deletion error", e);
                }
            }
        }
        dropletRepository.deleteAll(deletionList);
    }

    @Value("${cloudy.base-jar-directory}")
    private void setBaseJarDirectory(String dir) {
        File file = new File(dir);
        file.mkdirs();
        baseJarDirectory = file;
        logger.info("Jar directory: " + baseJarDirectory.getAbsolutePath());
    }

    @Value("${cloudy.base-storage-directory}")
    private void setBaseStorageDirectory(String dir) {
        File file = new File(dir);
        file.mkdirs();
        baseStorageDirectory = file;
        logger.info("Storage directory: " + baseStorageDirectory.getAbsolutePath());
    }
}
