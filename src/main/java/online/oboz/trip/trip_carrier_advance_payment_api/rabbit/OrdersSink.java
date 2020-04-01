package online.oboz.trip.trip_carrier_advance_payment_api.rabbit;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface OrdersSink {
    String ADVANCE_CREATE = "advance-create";

    @Output(ADVANCE_CREATE)
    MessageChannel create();
}
