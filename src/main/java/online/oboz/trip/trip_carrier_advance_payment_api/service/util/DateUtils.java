package online.oboz.trip.trip_carrier_advance_payment_api.service.util;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Value;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public interface DateUtils {
    String def_pattern = "E, dd.MM.yyyy в HH:mm:ss";

    static String format(OffsetDateTime dateTime) {
        return format(dateTime, def_pattern);
    }

    static String format(OffsetDateTime dateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).
            withZone(ZoneId.of("Europe/Moscow")).
            withLocale(new Locale("ru")).
            format(dateTime);
    }
}
