package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для работы с контрагентами
 */
@Service
public class AdvanceContractorService implements ContractorService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceContractorService.class);

    private final ContractorRepository contractorRepository;
    private final Long minPaidAdvancesCount;

    @Autowired
    public AdvanceContractorService(
        @Value("${services.auto-advance-service.min-paid-advance-count}") Long minAdvanceCount,
        ContractorRepository contractorRepository
    ) {
        this.minPaidAdvancesCount = minAdvanceCount;
        this.contractorRepository = contractorRepository;
    }

    @Override
    public AdvanceContractor findContractor(Long contractorId) {
        return contractorRepository.findById(contractorId).
            orElseThrow(() -> getContractorError("Contractor not found."));
    }


    @Override
    public Boolean isVatPayer(Long contractorId) {
        AdvanceContractor contractor = findContractor(contractorId);
        return contractor.getVatPayer();
    }

    @Override
    public ResponseEntity<List<AdvanceContractor>> updateAutoAdvanceForContractors() {
        List<AdvanceContractor> contractors = getAutoContractors();
        int count = contractors.size();
        if (count > 0) {
            log.info("[Auto-advance]: Found {} contractors to set 'auto-advance' flag for them.", count);
            contractors = setAutoForContractors(contractors);
        } else {
            log.info("[Auto-advance]: Contractors to set 'auto-advance' flag for them not found.");
        }
        return new ResponseEntity<>(contractors, HttpStatus.OK);
    }

    @Override
    public AdvanceContractor setAuto(AdvanceContractor contractor, Boolean flag) {
        contractor.setAutoContractor(flag);
        saveContractor(contractor);
        return contractor;
    }


    private List<AdvanceContractor> getAutoContractors() {
        List<AdvanceContractor> contractors = null;
        try {
            contractors = contractorRepository.findByMinCountAdvancesPaid(minPaidAdvancesCount);
        } catch (Exception e) {
            log.error("Error while getAutoContractors. " + e.getMessage());
        }
        return contractors;
    }


    private List<AdvanceContractor> setAutoForContractors(List<AdvanceContractor> contractors) {
        try {
            contractors.forEach(contractor -> {
                setAuto(contractor, true);
            });
        } catch (Exception e) {
            log.error("Error while updating auto-advance-contractors. " + e.getMessage());
        }
        return contractors;
    }

    public AdvanceContractor saveContractor(AdvanceContractor contractor) {
        contractorRepository.save(contractor);
        return contractor;
    }


    private BusinessLogicException getContractorError(String message) {
        return ErrorUtils.getInternalError("Contractor-service internal error: " + message);
    }
}
