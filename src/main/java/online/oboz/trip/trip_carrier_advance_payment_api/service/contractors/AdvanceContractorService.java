package online.oboz.trip.trip_carrier_advance_payment_api.service.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.Contractor;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class AdvanceContractorService implements ContractorService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceContractorService.class);

    private final ApplicationProperties applicationProperties;
    private final ContractorRepository contractorRepository;

    public AdvanceContractorService(ApplicationProperties applicationProperties, ContractorRepository contractorRepository, PersonRepository personRepository) {
        this.applicationProperties = applicationProperties;
        this.contractorRepository = contractorRepository;
    }

    @Override
    public AdvanceContractor findContractor(Long contractorId) {
        return contractorRepository.findById(contractorId).orElseThrow(() -> getContractorError(
            "Contractor not found."
        ));
    }


    @Override
    public Boolean isVatPayer(Long contractorId) {
        AdvanceContractor contractor = findContractor(contractorId);
        return contractor.getVatPayer();
    }

    @Override
    public ResponseEntity<List<AdvanceContractor>> updateAutoAdvanceForContractors() {
        List<AdvanceContractor> contractors = setAutoForContractors(getAutoContractors());
        return new ResponseEntity<>(contractors, HttpStatus.OK);
    }


    private List<AdvanceContractor> getAutoContractors() {
        List<AdvanceContractor> contractors = null;
        try {
            Long x = applicationProperties.getMinAdvanceCount();
            contractors = contractorRepository.findByMinCountAdvancesPaid(x);
        } catch (Exception e) {
            log.error("Error while getAutoContractors. " + e.getMessage());
        }
        return contractors;
    }


    private List<AdvanceContractor> setAutoForContractors(List<AdvanceContractor> contractors) {
        try {
            contractors.forEach(contractor -> {
                contractor.setAutoContractor(true);
                contractorRepository.save(contractor);
                log.info("Advance Contractor with id: {} set 'auto-advance' contractor.", contractor.getId());
            });
        } catch (Exception e) {
            log.error("Error while updating auto-advance-contractors. " + e.getMessage());
        }
        return contractors;
    }


    private BusinessLogicException getContractorError(String message) {
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }

    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("PersonsService - Business Error: " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }
}
