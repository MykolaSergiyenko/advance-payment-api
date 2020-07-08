package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.unf;

import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.Message;
import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.RabbitMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Integration1cService implements UnfService {
    private static final Logger log = LoggerFactory.getLogger(Integration1cService.class);

    private final RabbitMessageProducer rabbitMessageProducer;

    @Autowired
    public Integration1cService(RabbitMessageProducer rabbitMessageProducer) {
        this.rabbitMessageProducer = rabbitMessageProducer;
    }

    public void send1cNotification(Long advanceId) {
        rabbitMessageProducer.sendMessage(new Message(String.valueOf(advanceId)));
        log.info("Success send notification message to Rabbit advanceRequestId - " + advanceId);
    }
}
