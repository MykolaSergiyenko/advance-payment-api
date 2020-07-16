package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Сервис "Авансирование" - страница перевозчика
 */
public interface CarrierService {
    /**
     * Запрос страницы "Аванса" для перевозчика
     * @param uuid - аванс
     * @return CarrierPage - страница
     */
    ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid);
}
