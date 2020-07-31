package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SendSmsRequest;

/**
 * Интерфейс формирования текста сообщений по "Заявке на аванс"
 * {@link Advance}.
 */
public interface MessagesService {

    /**
     * @param advance
     * @return SmsContainer sms-message
     */
    SendSmsRequest createSms(Advance advance, String to) throws MessagingException;

    /**
     * @param advance
     * @param to
     * @return EmailContainer email-message
     */
    EmailContainer createEmail(Advance advance, String to) throws MessagingException;
}
