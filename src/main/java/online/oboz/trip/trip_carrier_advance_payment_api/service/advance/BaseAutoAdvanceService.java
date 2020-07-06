package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import org.springframework.http.ResponseEntity;

public interface BaseAutoAdvanceService {

    void updateFileUuid();

    void updateAutoAdvanceForContractors();

    void createAutoAdvances();

    void notifyAgain();


}
