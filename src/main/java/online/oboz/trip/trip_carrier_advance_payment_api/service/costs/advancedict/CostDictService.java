package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;

/**
 * Сервис для работы со "Справочником «Сумм аванса с НДС» и «Сборов за оформление документов»
 */
public interface CostDictService {

    /**
     * Получить запись справочника по стоимости поездки с НДС
     * @param ndsCost - стоимость с НДС
     * @return AdvanceCostDict - запись справочника
     */
    AdvanceCostDict findAdvanceSumByCost(Double ndsCost);

    Double findMinCost();

    Double findMaxCost();

}
