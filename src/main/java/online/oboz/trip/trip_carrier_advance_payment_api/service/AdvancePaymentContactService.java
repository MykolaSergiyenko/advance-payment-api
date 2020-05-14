package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.AdvanceContact;
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

    public AdvanceContact getAdvancePaymentContact(Long contractorId) {
        return advanceContactRepository.find(contractorId).orElse(new AdvanceContact());
    }
}
