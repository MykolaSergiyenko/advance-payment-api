package online.oboz.trip.trip_carrier_advance_payment_api.service.costs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict.AdvanceCostDictService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats.VatCostService;
import org.springframework.stereotype.Service;

@Service
public class AdvanceCostService implements CostService {

    private final ContractorService contractorService;
    private final VatCostService vatCostService;
    private final AdvanceCostDictService costDictService;

    public AdvanceCostService(
        ContractorService contractorService,
        VatCostService vatCostService, AdvanceCostDictService costDictService) {
        this.contractorService = contractorService;
        this.vatCostService = vatCostService;
        this.costDictService = costDictService;
    }

    @Override
    public Double calculateNdsCost(Double tripCost, String vatCode, Boolean isVatPayer) {
        Double ndsCost = tripCost + (isVatPayer ? (tripCost * getVatValue(vatCode)) : 0);
        return ndsCost;
    }

    public Advance setSumsToAdvance(Advance advance, Trip trip) {
        Long contractorId = trip.getContractorId();
        Double cost = trip.getTripCostInfo().getCost();
        String vatCode = trip.getVatCode();
        Boolean isVatPayer = contractorService.isVatPayer(contractorId);

        TripCostInfo costInfo = new TripCostInfo(calculateNdsCost(cost, vatCode, isVatPayer));
        advance.setCostInfo(costInfo);

        AdvanceCostDict dict = costDictService.findAdvanceSumByCost(costInfo.getCost());
        AdvanceInfo info = new AdvanceInfo(dict.getAdvancePaymentSum(), dict.getRegistrationFee());
        advance.setTripAdvanceInfo(info);

        return advance;
    }


    private Double getVatValue(String code) {
        return vatCostService.getVatValue(code);
    }
}
