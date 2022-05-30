package lebedev.locator;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Beacon {
    @Id
    private String id;

    private Date created;

    private byte[] data;

    public Beacon() {
        created = new Date();
    }

    public Beacon(String id) {
        this.id = id;
        this.data = new byte[0];
        created = new Date();
    }

    public String getId() {
        return this.id;
    }

    public Date getCreated() {
        return created;
    }

    public void appendData(byte[] data) {
        byte[] oldData = this.data;
        this.data = new byte[this.data.length + data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        System.arraycopy(oldData, 0, this.data, data.length, oldData.length);
    }

    public InputStream getDataReader() {
        return new ByteArrayInputStream(this.data);
    }

    public static BeaconEntry parseEntry(InputStream reader) throws IOException {
        BeaconEntry entry = new BeaconEntry();
        entry.timestamp = readString(reader);
        Location loc = new Location();
        loc.lat = readFloat(reader);
        loc.lon = readFloat(reader);
        String locationFormat = readString(reader);
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(locationFormat, new TemplateParserContext());
            StandardEvaluationContext context = new StandardEvaluationContext(loc);
            entry.location = exp.getValue(context, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            entry.location = "<error>";
        }
        entry.comment = readString(reader);
        return entry;
    }

    public List<BeaconEntry> parseEntries() {
        InputStream reader = this.getDataReader();
        List<BeaconEntry> entries = new ArrayList<>();
        while (true) {
            try {
                byte[] len = reader.readNBytes(1);
                if (len.length < 1) {
                    return entries;
                }
                int length = len[0];
                byte[] entry = reader.readNBytes(length);
                if (entry.length < length) {
                    return entries;
                }
                entries.add(parseEntry(new ByteArrayInputStream(entry)));
                if (reader.available() == 0) {
                    return entries;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return entries;
            }
        }
    }

    private static String readString(InputStream reader) throws IOException {
        int len = readInt(reader);
        if (len < 0) {
            return "";
        }
        byte[] data = reader.readNBytes(len);
        return new String(data);
    }

    private static int readInt(InputStream reader) throws IOException {
        byte[] data = reader.readNBytes(4);
        if (data.length < 4) {
            return 0;
        }
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static float readFloat(InputStream reader) throws IOException {
        byte[] data = reader.readNBytes(4);
        if (data.length < 4) {
            return 0;
        }
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static class Location {
        public float lat;
        public float lon;
    }

    public static class BeaconEntry {
        public String timestamp;
        public String location;
        public String comment;
    }
}
