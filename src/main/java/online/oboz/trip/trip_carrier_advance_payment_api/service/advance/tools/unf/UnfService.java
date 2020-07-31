package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.unf;

/**
 * Сервис отправки сообщений в УНФ
 */
public interface UnfService {

    /**
     * Отправить аванс в УНФ
     * @param advanceId
     */
    void send1cNotification(Long advanceId);
}
