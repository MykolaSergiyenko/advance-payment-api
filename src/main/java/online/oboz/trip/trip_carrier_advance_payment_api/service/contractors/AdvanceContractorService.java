package online.oboz.trip.trip_carrier_advance_payment_api.service.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvanceContractorService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceContractorService.class);

    private final ApplicationProperties applicationProperties;
    private final ContractorRepository contractorRepository;

    public AdvanceContractorService(ApplicationProperties applicationProperties, ContractorRepository contractorRepository, PersonRepository personRepository) {
        this.applicationProperties = applicationProperties;
        this.contractorRepository = contractorRepository;
    }

    public void updateAutoAdvanceForContractors() {
        long x = applicationProperties.getMinAdvanceCount();
        List<AdvanceContractor> candidates = contractorRepository.findByMinCountAdvancesPaid(x);
        candidates.forEach(contractor -> {
            contractor.setAutoContractor(true);
            contractorRepository.save(contractor);
            log.info("Advance Contractor with id: {} set 'auto-advance' contractor.", contractor.getId());
        });
    }
}
