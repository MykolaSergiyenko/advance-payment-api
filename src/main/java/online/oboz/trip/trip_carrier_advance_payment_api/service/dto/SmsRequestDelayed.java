package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class SmsRequestDelayed extends SendSmsRequest implements Delayed {

    private final long time;

    public SmsRequestDelayed(String messageText, String phone, int time) {
        super(messageText, phone);
        this.time = System.currentTimeMillis() + time;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = time - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed obj) {
        return Long.compare(this.time, ((SmsRequestDelayed) obj).time);
    }
}
