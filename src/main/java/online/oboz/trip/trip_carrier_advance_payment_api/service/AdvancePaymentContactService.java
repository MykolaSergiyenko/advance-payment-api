package online.oboz.trip.trip_carrier_advance_payment_api.service;

import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorAdvancePaymentContactRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdvancePaymentContactService {
    private final ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository;

    public AdvancePaymentContactService(
        ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository
    ) {
        this.contractorAdvancePaymentContactRepository = contractorAdvancePaymentContactRepository;
    }

    public ContractorAdvancePaymentContact getAdvancePaymentContact(Long contractorId) {
        return contractorAdvancePaymentContactRepository
            .findContractorAdvancePaymentContact(contractorId).orElse(new ContractorAdvancePaymentContact());
    }
}
