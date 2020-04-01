package online.oboz.trip.trip_carrier_advance_payment_api.rabbit;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private final String advanceId;

    public Message(String advanceId) {
        this.advanceId = advanceId;
    }

    public Map<String, String> getPayload() {
        Map<String, String> msg = new HashMap<>();
        msg.put("dt", OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        msg.put("advance_id", advanceId);
        msg.put("entityType", "ADVANCE");

        return msg;
    }
}
