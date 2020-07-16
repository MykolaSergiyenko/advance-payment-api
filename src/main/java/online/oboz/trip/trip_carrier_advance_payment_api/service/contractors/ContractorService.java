package online.oboz.trip.trip_carrier_advance_payment_api.service.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Сервис для работы с контрагентами
 */
public interface ContractorService {

    /**
     * Получить контрагента по id
     * @param contractorId
     * @return AdvanceContractor - контрагент
     */
    AdvanceContractor findContractor(Long contractorId);

    /**
     * Обновить признак "Авто-авансирование" у контрагентов
     * @return List<AdvanceContractor> - список контрагентов
     */
    ResponseEntity<List<AdvanceContractor>> updateAutoAdvanceForContractors();

    /**
     * Является ли контрагент плательщиком налогов
     * @param contractorId - контрагент
     * @return true\false
     */
    Boolean isVatPayer(Long contractorId);
}
