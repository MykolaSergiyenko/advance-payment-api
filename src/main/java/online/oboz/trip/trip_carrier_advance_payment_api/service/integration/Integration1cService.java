package online.oboz.trip.trip_carrier_advance_payment_api.service.integration;

import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.Message;
import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.RabbitMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Integration1cService {
    private static final Logger log = LoggerFactory.getLogger(Integration1cService.class);

    private final RabbitMessageProducer rabbitMessageProducer;

    public Integration1cService(RabbitMessageProducer rabbitMessageProducer) {
        this.rabbitMessageProducer = rabbitMessageProducer;
    }

    public void send1cNotification(long advanceRequestId) {
        rabbitMessageProducer.sendMessage(new Message(String.valueOf(advanceRequestId)));
        log.info("Success send notification message to Rabbit advanceRequestId - " + advanceRequestId);
    }
}
