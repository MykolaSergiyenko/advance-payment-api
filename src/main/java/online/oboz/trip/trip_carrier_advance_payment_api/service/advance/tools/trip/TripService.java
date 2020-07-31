package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.TripAdvanceState;

import java.util.List;

/**
 * Сервис для работы с "Поездками"
 */
public interface TripService {

    /**
     * Рассчетная стоимость триап с НДС
     *
     * @param trip
     * @return
     */
    Double calculateTripCost(Trip trip);

    /**
     * Минимальная возможная стоимость поездки с НДС
     *
     * @return минимальная возможная стоимость поездки с НДС
     */
    Double getTripMinCost();

    /**
     * Максимальная возможная стоимость поездки с НДС
     *
     * @return максимальная возможная стоимость поездки с НДС
     */
    Double getTripMaxCost();

    /**
     * Получить запись справочника «Сумм аванса с НДС» и «Сборов за оформление документов»
     *
     * @param cost - стоимость поездки с НДС
     * @return AdvanceCostDict - строка справочника
     */
    AdvanceCostDict getAdvanceDictByCost(Double cost);

    /**
     * Найти трип
     *
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
     *
     * @return
     */
    List<Trip> getAutoAdvanceTrips();

    /**
     * Установить сумму аванса и сбор по данным поездки
     *
     * @param advance - аванс
     * @param trip    - поездка
     * @return advance - аванс
     */
    Advance setSumsToAdvance(Advance advance, Trip trip);

    /**
     * Статус выдачи аванса для поездки
     *
     * @param trip - поездка
     * @return tooltip - подсказка
     */
    TripAdvanceState checkTripAdvanceState(Trip trip);
}
