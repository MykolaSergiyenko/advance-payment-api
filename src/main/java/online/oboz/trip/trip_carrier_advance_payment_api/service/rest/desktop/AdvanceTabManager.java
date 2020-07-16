package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Сервис "Авансирование" для вкладок рабочего стола
 */
public interface AdvanceTabManager {
    Logger log = LoggerFactory.getLogger(AdvanceTabManager.class);

    /**
     * Авансы "В работе"
     * @param filter
     * @return AdvanceDesktopDTO - список авансов в гриде рабочего стола
     */
    ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter);

    /**
     * "Проблемные" авансы
     */
    ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter);

    /**
     * "Оплаченные" авансы
     */
    ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter);

    /**
     * "Неоплаченные" авансы
     */
    ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter);

    /**
     * "Отмененные" авансы
     */
    ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter);
}
