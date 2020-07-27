package online.oboz.trip.trip_carrier_advance_payment_api.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public interface DateUtils {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("в E, dd.MM.yyyy в HH:mm:ss.SSS").
        withLocale(new Locale("ru"));

    static String format(OffsetDateTime dateTime) {
        return formatter.format(dateTime);
    }
}
