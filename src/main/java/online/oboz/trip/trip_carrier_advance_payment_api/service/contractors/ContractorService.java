package online.oboz.trip.trip_carrier_advance_payment_api.service.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ContractorService {

    AdvanceContractor findContractor(Long contractorId);

    ResponseEntity<List<AdvanceContractor>> updateAutoAdvanceForContractors();

    Boolean isVatPayer(Long contractorId);
}
