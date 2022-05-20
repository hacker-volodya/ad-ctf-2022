package lebedev.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

@Component
public class ScheduledTasks {
    private final BeaconRepository BeaconRepository;
    private final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    public ScheduledTasks(BeaconRepository BeaconRepository) {
        this.BeaconRepository = BeaconRepository;
    }

    @Scheduled(fixedRate = 15000)
    public void cleanOldBeacons() {
        ArrayList<Beacon> deletionList = new ArrayList<>();
        for (Beacon beacon : BeaconRepository.findAll()) {
            long duration = (new Date()).getTime() - beacon.getCreated().getTime();
            if (duration > 15 * 60 * 1000) {
                deletionList.add(beacon);
                logger.info("Deleting old beacon " + beacon.getId() + " (deployed on " + beacon.getCreated() + ")");
            }
        }
        BeaconRepository.deleteAll(deletionList);
    }
}
