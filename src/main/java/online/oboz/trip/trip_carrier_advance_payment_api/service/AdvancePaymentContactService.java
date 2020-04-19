package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactRepository;
import org.springframework.stereotype.Service;

@Service
public class AdvancePaymentContactService {
    private final AdvanceContactRepository advanceContactRepository;

    public AdvancePaymentContactService(
        AdvanceContactRepository advanceContactRepository
    ) {
        this.advanceContactRepository = advanceContactRepository;
    }

    public ContractorAdvancePaymentContact getAdvancePaymentContact(Long contractorId) {
        return advanceContactRepository.find(contractorId).orElse(new ContractorAdvancePaymentContact());
    }
}
