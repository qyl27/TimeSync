package cx.rain.mc.timesync.utility;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class FetchHelper {
    private static final Gson GSON = new Gson();

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
}
