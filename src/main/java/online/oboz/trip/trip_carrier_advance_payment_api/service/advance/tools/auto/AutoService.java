package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.auto;


/**
 * Под-сервис "Авто-авансирование"
 */
public interface AutoService {

//    void updateFileUuid();

    void updateAutoAdvanceContractors();

    void createAutoAdvances();

    void notifyAgain();


}
