package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceCostDictRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

    private BusinessLogicException getCostDictError(String message) {
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }

    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("PersonsService - Business Error: " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }
}