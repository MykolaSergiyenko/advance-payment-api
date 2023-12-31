package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.reports;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

/**
 * Сервис для работы с шаблонами\отчетами
 *
 * @author s‡oodent
 */
public interface ReportService {

    /**
     * Загрузить шаблон "Заявки на аванс"
     * @param advance - аванс
     * @return Resource - pdf-шаблон
     */
    ResponseEntity<Resource> downloadAdvanceTemplate(Advance advance);
}
