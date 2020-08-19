package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contractors;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Сервис для работы с контрагентами
 *
 * @author s‡udent
 */
public interface ContractorService {

    /**
     * Получить контрагента по id
     *
     * @param contractorId
     * @return AdvanceContractor - контрагент
     */
    AdvanceContractor findContractor(Long contractorId);


    /**
     * Установить значение флага "авто-аванс" контрагенту
     *
     * @param contractor контрагент
     * @param flag       флаг
     * @return
     */
    AdvanceContractor setAuto(AdvanceContractor contractor, Boolean flag);


    /**
     * Обновить признак "Авто-авансирование" у контрагентов
     *
     * @return List<AdvanceContractor> - список контрагентов
     */
    ResponseEntity<List<AdvanceContractor>> updateAutoAdvanceForContractors();

    /**
     * Является ли контрагент плательщиком nologov
     *
     * @param contractorId - контрагент
     * @return true\false
     */
    Boolean isVatPayer(Long contractorId);


    AdvanceContractor saveContractor(AdvanceContractor advance);
}
