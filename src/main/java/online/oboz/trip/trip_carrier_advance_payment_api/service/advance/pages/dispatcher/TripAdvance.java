package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.pages.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.TripAdvanceApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.TripAdvanceState;
import org.springframework.http.ResponseEntity;

/**
 * Сервис "Авансирование" - карточка Трипа
 *
 * @author s‡udent
 */
public interface TripAdvance extends TripAdvanceApiDelegate {

    /**
     * Статус выдачи аванса по трипу
     *
     * @param tripId
     * @return IsTripAdvanced - DTO статуса выдачи
     */
    ResponseEntity<TripAdvanceState> getAdvanceState(Long tripId);

    /**
     * Выдать аванс по трипу
     *
     * @param tripId
     */
    ResponseEntity<Void> giveAdvanceForTrip(Long tripId);

}
