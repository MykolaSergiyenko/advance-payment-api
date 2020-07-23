package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Сервис для управления "Авансами"
 */
public interface AdvanceManager {
    Logger log = LoggerFactory.getLogger(AdvanceManager.class);

    /**
     * Утвердить аванс (отправить в УНФ)
     * @param advanceId - аванс
     */
    ResponseEntity<Void> confirmAdvancePayment(Long advanceId);

    /**
     * Отменить аванс по Трипу
     * @param tripId - трип
     * @param cancelComment - комментарий
     */
    ResponseEntity<Void> cancelAdvancePayment(Long tripId, String cancelComment);

    /**
     * Обновить факт загрузки водителя
     * @param advanceId - аванс
     * @param loadingComplete - значение признака
     */
    ResponseEntity<Void> updateLoadingComplete(Long advanceId, Boolean loadingComplete);

    /**
     * Изменить комментарий в авансе
     * @param comment - структура данных комментария
     */
    ResponseEntity<Void> changeAdvancePaymentComment(AdvanceCommentDTO comment);


    void checkAccess();
}
