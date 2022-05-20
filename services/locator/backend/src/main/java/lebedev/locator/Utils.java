package lebedev.locator;

import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.json.Mapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Utils {
    public static String signJwt(JWT jwt, String secret) {
        HMACSigner signer = HMACSigner.newSHA256Signer(secret);
        List<String> parts = new ArrayList<>(3);
        Header header = new Header();
        header.algorithm = signer.getAlgorithm();
        Base64.Encoder b64encoder = Base64.getUrlEncoder().withoutPadding();
        parts.add(new String(b64encoder.encode(Mapper.serialize(header))));
        parts.add(new String(b64encoder.encode(Mapper.serialize(jwt.toString()))));
        byte[] signature = signer.sign(String.join(".", parts));
        parts.add(new String(b64encoder.encode(signature)));
        return String.join(".", parts);
    }
}
