package online.oboz.trip.trip_carrier_advance_payment_api.service;

import org.junit.Test;

import java.time.OffsetDateTime;

public class DatePatternTest {
    @Test
    public void name() {
        OffsetDateTime date = OffsetDateTime.parse("2016-10-02T00:00:00+00:00");
        //OffsetDateTime data = OffsetDateTime.parse("2020-03-06", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        System.out.println(date);
    }
}
