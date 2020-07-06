package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;

public interface CostDictService {

    AdvanceCostDict findAdvanceSumByCost(Double ndsCost);

}
