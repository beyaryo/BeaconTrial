package id.developer.lynx.cubeacontry.bluetooth.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Bend on 10/15/2016.
 */

public class TimeFormater {
    private final static String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
    private final static SimpleDateFormat ISO_FORMATTER = new UtcDateFormater(ISO_FORMAT, Locale.US);

    public static String getIsoDateTime(final Date date) {
        return ISO_FORMATTER.format(date);
    }

    public static String getIsoDateTime(final long millis) {
        return getIsoDateTime(new Date(millis));
    }
}
