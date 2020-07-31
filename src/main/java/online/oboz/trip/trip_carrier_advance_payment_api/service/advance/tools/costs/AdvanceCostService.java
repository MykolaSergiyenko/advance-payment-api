package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs.vats.VatCostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdvanceCostService implements CostService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceCostService.class);


    private final ContractorService contractorService;
    private final VatCostService vatCostService;


    @Autowired
    public AdvanceCostService(
        ContractorService contractorService,
        VatCostService vatCostService
    ) {
        this.contractorService = contractorService;
        this.vatCostService = vatCostService;
    }

    @Override
    public Double calculateWithNdsForTrip(Trip trip) {
        Long contractorId = trip.getContractorId();
        Double cost = trip.getTripCostInfo().getCost();
        if (null == cost || cost == 0.0) throw getCostServiceError("Trip cost is null or zero.");
        String vatCode = trip.getVatCode();
        Boolean isVatPayer = contractorService.isVatPayer(contractorId);
        return calculateNdsCost(cost, vatCode, isVatPayer);
    }

    @Override
    public Double calculateNdsCost(Double tripCost, String vatCode, Boolean isVatPayer) {
        Double ndsCost = tripCost + (isVatPayer ? (tripCost * getVatValue(vatCode)) : 0);
        return ndsCost;
    }


    private Double getVatValue(String code) {
        return vatCostService.getVatValue(code);
    }

    private BusinessLogicException getCostServiceError(String message) {
        return ErrorUtils.getInternalError("Cost-service internal error: " + message);
    }
}
