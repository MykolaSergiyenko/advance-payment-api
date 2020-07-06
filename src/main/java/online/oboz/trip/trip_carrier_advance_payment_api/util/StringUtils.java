package online.oboz.trip.trip_carrier_advance_payment_api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringUtils {
    Logger log = LoggerFactory.getLogger(StringUtils.class);

    private StringUtils() {
    }

    public static Boolean isEmptyString(String s) {
        return (s == null || org.apache.commons.lang.StringUtils.isBlank(s));
    }

    public static Boolean isEmptyStrings(String... strings) {
        for (String s : strings) {
            if (isEmptyString(s)) return true;
        }
        return false;
    }
}
