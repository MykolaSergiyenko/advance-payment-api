package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats;


import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.VatsRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VatCostService implements VatService {

    private final VatsRepository vatsRepository;

    public VatCostService(VatsRepository vatsRepository) {
        this.vatsRepository = vatsRepository;
    }


    @Override
    public Double getVatValue(String vatCode) {
        return vatsRepository.findByCode(vatCode).
            orElseThrow(() -> getVatDictError("Vat-value not found by vat-code."));
    }

    @Override
    public List<String> getZeroCodes() {
        return vatsRepository.finZeroCodes();
    }

    private BusinessLogicException getVatDictError(String message) {
        return ErrorUtils.getInternalError("Vat-dict-service internal error: " + message);
    }
}
