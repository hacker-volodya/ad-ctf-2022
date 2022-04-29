package lebedev.cloudydays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class MainController {
    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final DropletRepository dropletRepository;

    private File baseJarDirectory;

    private File baseStorageDirectory;

    private DropletExecutor dropletExecutor;

    public MainController(DropletRepository dropletRepository) {
        this.dropletRepository = dropletRepository;
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

    @PostConstruct
    private void init() {
        this.dropletExecutor = new DropletExecutor(baseJarDirectory.getAbsolutePath(), baseStorageDirectory.getAbsolutePath());
    }

    @GetMapping("/droplets")
    Iterable<String> list() {
        return StreamSupport.stream(dropletRepository.findAll().spliterator(), true).map(Droplet::getName).collect(Collectors.toList());
    }

    @GetMapping("/droplets/{name}")
    Droplet get(@PathVariable String name) {
        return dropletRepository.findById(name).orElseThrow(() -> new DropletNotFoundException(name));
    }

    @PutMapping("/droplets/{name}")
    Droplet upload(@PathVariable String name, @RequestParam("file") MultipartFile file) throws IOException {
        File targetFile = new File(baseJarDirectory.getAbsolutePath() + File.separator + name + ".jar");
        logger.info("Saving droplet '" + name + "' to " + targetFile.getAbsolutePath());
        file.transferTo(targetFile);
        Droplet droplet = new Droplet(name);
        dropletRepository.save(droplet);
        return droplet;
    }

    @PostMapping("/droplets/{name}")
    String execute(@PathVariable String name, @RequestParam("arguments") String[] arguments) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Droplet droplet = dropletRepository.findById(name).orElseThrow(() -> new DropletNotFoundException(name));
        String result = dropletExecutor.execute(name, arguments);
        droplet.addLog(result);
        dropletRepository.save(droplet);
        return result;
    }
}
