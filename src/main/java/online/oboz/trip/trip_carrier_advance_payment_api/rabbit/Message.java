package online.oboz.trip.trip_carrier_advance_payment_api.rabbit;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private static final String ORDER_TRACKED_MESSAGE_TYPE = "order_tracked";

    private final String tripId;
    private final String statusCode;

    public Message(String tripId, String statusCode) {
        this.tripId = tripId;
        this.statusCode = statusCode;
    }

    public Map<String, String> getPayload() {
        Map<String, String> msg = new HashMap<>();
        msg.put("dt", OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        msg.put("status_code", statusCode);
        msg.put("trip_id", tripId);
        msg.put("message_type", ORDER_TRACKED_MESSAGE_TYPE);
        return msg;
    }
}
