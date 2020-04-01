package online.oboz.trip.trip_carrier_advance_payment_api.service;

import org.junit.Test;

import java.time.OffsetDateTime;

public class DatePatternTest {
    @Test
    public void name() {
        OffsetDateTime date = OffsetDateTime.parse("2016-10-02T00:00:00+00:00");
        System.out.println(date);
    }
}
