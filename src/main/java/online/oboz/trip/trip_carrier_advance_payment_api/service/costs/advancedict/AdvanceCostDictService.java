package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceCostDictRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class AdvanceCostDictService implements CostDictService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceCostDictService.class);


    private final AdvanceCostDictRepository costDictRepository;

    public AdvanceCostDictService(AdvanceCostDictRepository costDictRepository) {
        this.costDictRepository = costDictRepository;
    }

    public AdvanceCostDict findAdvanceSumByCost(Double ndsCost) {
        return costDictRepository.getAdvancePaymentCost(ndsCost).orElseThrow(() ->
            getCostDictError("Advance sum and fee dictionary record not found for cost:" + ndsCost));
    }

    public Double findMinCost(){
        return costDictRepository.getMinCost();
    }

    public Double findMaxCost(){
        return costDictRepository.getMaxCost();
    }

    private BusinessLogicException getCostDictError(String message) {
        return ErrorUtils.getInternalError("Cost-dict-service internal error: " + message);
    }
}
