package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import org.springframework.stereotype.Service;

/**
 * Сервис по расчету стоимости Поездки с НДС
 * и установке ценовых характеристик аванса
 */
@Service
public interface CostService {

    /**
     * Рассчитать стоимость поездки с НДС
     *
     * @param trip поездка
     * @return
     */
    Double calculateWithNdsForTrip(Trip trip);

    /**
     * Рассчитать стоимость поездки с НДС
     *
     * @param tripCost   - стоимость поездки
     * @param vatCode    - код НДС
     * @param isVatPayer - признак контрагента-плательщика НДС
     * @return Double - стоимость с учетом НДС
     */
    Double calculateNdsCost(Double tripCost, String vatCode, Boolean isVatPayer);

}
