package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

import java.util.List;

/**
 * Сервис для работы с "Поездками"
 */
public interface TripService {

    /**
     * Найти трип
     * @param tripId
     * @return Trip
     */
    Trip findTripById(Long tripId);

    /**
     * Найти трипы для автоаванса:
     * - у контрагента стоит признак Auto
     * - поездка типа 'motor' и в статусе 'назначена'
     * - стоимость поездки больше минимальной суммы
     * - статус трипа "назначен" проставлен позже "минимальной даты"
     * - аванс по трипу еще не выдан
     * @return
     */
    List<Trip> getAutoAdvanceTrips();


}
