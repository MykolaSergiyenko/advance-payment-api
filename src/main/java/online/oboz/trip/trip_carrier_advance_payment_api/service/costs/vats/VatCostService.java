package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats;


import online.oboz.trip.trip_carrier_advance_payment_api.repository.VatsRepository;
import org.springframework.stereotype.Service;

@Service
public class VatCostService implements VatService{

    private final VatsRepository vatsRepository;

    public VatCostService(VatsRepository vatsRepository) {
        this.vatsRepository = vatsRepository;
    }


    @Override
    public Double getVatValue(String vatCode) {
        return vatsRepository.findByCode(vatCode);
    }
}
