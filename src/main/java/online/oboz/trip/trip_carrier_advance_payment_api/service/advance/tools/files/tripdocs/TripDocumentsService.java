package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с документами-вложениями трипа
 *
 * @author s‡oodent
 */
public interface TripDocumentsService {

    /**
     * Получить все документы трипа
     *
     * @param tripId
     * @return List<TripAttachment> - список вложений
     */
    List<TripAttachment> getTripAttachments(Long tripId);

    /**
     * Получить UUID "Заявки" или "Договора-заявки" из списка документов
     *
     * @param attachments - список вложений
     * @return UUID - "Заявки" или "Договора-заявки"
     */
    UUID getRequestUuidOrTripRequestUuid(List<TripAttachment> attachments);

    /**
     * Получить UUID "Заявки" из списка документов
     *
     * @param attachments - список вложений
     * @return UUID - "Заявки"
     */
    UUID getRequestUuid(List<TripAttachment> attachments);

    /**
     * Получить UUID "Договора-заявки" из списка документов
     *
     * @param attachments - список вложений
     * @return UUID - "Договора-заявки"
     */
    UUID getTripRequestUuid(List<TripAttachment> attachments);

    /**
     * Получить UUID подписанного вложения "Заявка на авансирование" из списка документов
     *
     * @param attachments - список вложений
     * @return UUID - подписанного вложения "Заявка на авансирование"
     */
    UUID getAssignmentRequestUuid(List<TripAttachment> attachments);

    /**
     * Сохранить (создать запись) в "Документах" по трипу
     * с подписанным вложением типа "Заявка на авансирование"
     *
     * @param tripId   - трип
     * @param fileUuid - uuid вложения из bstore
     * @return
     */
    UUID saveAssignmentAdvanceRequestUuid(Long tripId, UUID fileUuid);

    /**
     * Загружены ли все документы по авансу - в трип?
     *
     * @param advance - аванс
     * @return true\false
     */
    Boolean isAllDocumentsLoaded(Advance advance);

    /**
     * Загружены ли все документы в Трип
     *
     * @param tripId - трип
     * @return true\false
     */
    Boolean isAllTripDocumentsLoaded(Long tripId);
}
