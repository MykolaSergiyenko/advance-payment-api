package online.oboz.trip.trip_carrier_advance_payment_api.service.costs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

/**
 * Сервис по расчету стоимости Поездки с НДС
 * и установке ценовых характеристик аванса
 */
public interface CostService {

    /**
     * Рассчитать стоимость поездки с НДС
     * @param tripCost - стоимость поездки
     * @param vatCode - код НДС
     * @param isVatPayer - признак контрагента-плательщика НДС
     * @return Double - стоимость с учетом НДС
     */
    Double calculateNdsCost(Double tripCost, String vatCode, Boolean isVatPayer);

    /**
     * Установить сумму аванса и сбор по данным поездки
     * @param advance - аванс
     * @param trip - поездка
     * @return advance - аванс
     */
    Advance setSumsToAdvance(Advance advance, Trip trip);
}
