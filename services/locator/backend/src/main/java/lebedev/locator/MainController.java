package lebedev.locator;

import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import io.fusionauth.jwt.json.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class MainController {
    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Value("${locator.secret}")
    private String jwtSecret;

    private final BeaconRepository beaconRepository;

    public MainController(BeaconRepository beaconRepository) {
        this.beaconRepository = beaconRepository;
    }

    @GetMapping("/beacons")
    List<String> listBeacons() {
        return StreamSupport.stream(beaconRepository.findAll().spliterator(), true).map(Beacon::getId).collect(Collectors.toList());
    }

    @GetMapping("/beacons/{id}")
    List<String> getBeaconPublic(@PathVariable String id) {
        Beacon beacon = beaconRepository.findById(id).orElseThrow();
        List<Beacon.BeaconEntry> entries = beacon.parseEntries();
        return entries.parallelStream().map(x -> x.timestamp).collect(Collectors.toList());
    }

    @PutMapping("/beacons/{id}")
    String createBeacon(@PathVariable String id) {
        if (beaconRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already exists");
        }
        Beacon beacon = new Beacon(id);
        beaconRepository.save(beacon);
        JWT jwt = new JWT();
        jwt.addClaim("id", id);
        return Utils.signJwt(jwt, jwtSecret);
    }

    @GetMapping("/beacons/{id}/private")
    List<Beacon.BeaconEntry> getBeacon(@PathVariable String id, @RequestHeader("X-Auth") String token) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] parts = token.split("\\.");
        String payload = new String(decoder.decode(parts[1]));
        payload = payload.subSequence(1, payload.length() - 1).toString();
        payload = payload.replace("\\r", "").replace("\\n", "").replace("\\\"", "\"");
        JWT jwt = Mapper.deserialize(payload.getBytes(StandardCharsets.UTF_8), JWT.class);
        HMACVerifier verifier = HMACVerifier.newVerifier(jwtSecret);
        verifier.verify(Algorithm.HS256, (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8), decoder.decode(parts[2]));
        if (!id.equals(jwt.getString("id"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad beacon id for current token");
        }
        Beacon beacon = beaconRepository.findById(id).orElseThrow();
        return beacon.parseEntries();
    }

    @PostMapping("/beacons/{id}")
    String reportBeaconLocation(@RequestBody() byte[] data, @PathVariable String id) {
        if (data.length > 256) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many data!");
        }
        Beacon beacon = beaconRepository.findById(id).orElseThrow();
        beacon.appendData(data);
        beaconRepository.save(beacon);
        return "ok";
    }
}
