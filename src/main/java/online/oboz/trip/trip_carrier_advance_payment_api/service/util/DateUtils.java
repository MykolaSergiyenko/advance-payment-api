package online.oboz.trip.trip_carrier_advance_payment_api.service.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public interface DateUtils {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" E, dd.MM.yyyy в HH:mm:ss.SSS").
        withZone(ZoneId.of("Europe/Moscow")).
        withLocale(new Locale("ru"));

    static String format(OffsetDateTime dateTime) {
        return formatter.format(dateTime);
    }
}
