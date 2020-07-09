package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.auto;


public interface AutoService {

    void updateFileUuid();

    void updateAutoAdvanceForContractors();

    void createAutoAdvances();

    void notifyAgain();


}
