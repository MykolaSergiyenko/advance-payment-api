package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorContactRepository;
import org.springframework.stereotype.Service;

@Service
public class AdvancePaymentContactService {
    private final ContractorContactRepository contractorContactRepository;

    public AdvancePaymentContactService(
        ContractorContactRepository contractorContactRepository
    ) {
        this.contractorContactRepository = contractorContactRepository;
    }

    public ContractorAdvancePaymentContact getAdvancePaymentContact(Long contractorId) {
        return contractorContactRepository.find(contractorId).orElse(new ContractorAdvancePaymentContact());
    }
}
