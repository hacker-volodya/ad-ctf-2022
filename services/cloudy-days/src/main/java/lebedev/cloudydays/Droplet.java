package lebedev.cloudydays;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Droplet {
    @Id
    private String name;

    private Date created;

    @ElementCollection
    private List<String> logs;

    public Droplet() {
    }

    public Droplet(String name) {
        this.name = name;
        created = new Date();
        logs = new ArrayList<>();
    }

    public List<String> getLogs() {
        return logs;
    }

    public Date getCreated() {
        return created;
    }

    public String getName() {
        return name;
    }

    public void addLog(String log) {
        logs.add(log);
    }
}
