package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.unf;

import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.Message;
import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.RabbitMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис по отправке "Аванса" в очередь заявок УНФ
 */
@Service
public class Integration1cService implements UnfService {
    private static final Logger log = LoggerFactory.getLogger(Integration1cService.class);

    private final RabbitMessageProducer rabbitMessageProducer;

    public Integration1cService(RabbitMessageProducer rabbitMessageProducer) {
        this.rabbitMessageProducer = rabbitMessageProducer;
    }

    public void send1cNotification(Long advanceId) {
        rabbitMessageProducer.sendMessage(new Message(String.valueOf(advanceId)));
        log.info("[Аванс]: {} успешно отправлен в Rabbit-очередь УНФ.", advanceId);
    }
}
