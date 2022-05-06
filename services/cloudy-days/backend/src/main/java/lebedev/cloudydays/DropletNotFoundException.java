package lebedev.cloudydays;

public class DropletNotFoundException extends RuntimeException {
    public DropletNotFoundException(String name) {
        super("Can't find droplet " + name);
    }
}
