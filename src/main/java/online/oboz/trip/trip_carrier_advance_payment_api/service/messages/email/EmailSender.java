package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message.MessagingException;

/**
 * Интерфейс сервиса отправления электронной почты
 */
public interface EmailSender {

    /**
     * @param email Отправить и-мейл
     */
    void sendEmail(EmailContainer email) throws MessagingException;
}
