package online.oboz.trip.trip_carrier_advance_payment_api.rabbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(OrdersSink.class)
public class RabbitMessageProducer {

    private final OrdersSink ordersSink;

    @Autowired
    public RabbitMessageProducer(OrdersSink ordersSink) {
        this.ordersSink = ordersSink;
    }

    public void sendMessage(Message msg) {
        ordersSink.update().send(buildMessage(msg.getPayload()));
    }

    private static final <T> org.springframework.messaging.Message buildMessage(T val) {
        return MessageBuilder.withPayload(val).build();
    }
}
