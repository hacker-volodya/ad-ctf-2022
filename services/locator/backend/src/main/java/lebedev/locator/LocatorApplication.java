package lebedev.locator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

@SpringBootApplication
@EnableScheduling
public class LocatorApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SpringApplication.run(LocatorApplication.class, args);
    }

}
