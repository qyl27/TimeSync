package cx.rain.mc.timesync.utility;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.zip.GZIPInputStream;

public class FetchHelper {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
            .create();

    public static <T> T fetchJson(String url, Class<T> model, boolean gzip) {
        try {
            var u = new URL(url);
            var connection = u.openConnection();
            var is = connection.getInputStream();
            if (gzip) {
                is = new GZIPInputStream(is);
            }
            return GSON.fromJson(new BufferedReader(new InputStreamReader(is)), model);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class OffsetDateTimeTypeAdapter
            implements JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {
        @Override
        public OffsetDateTime deserialize(JsonElement json, Type typeOfT,
                                         JsonDeserializationContext context) throws JsonParseException {
            return OffsetDateTime.parse(json.getAsString());
        }

        @Override
        public JsonElement serialize(OffsetDateTime src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
}
