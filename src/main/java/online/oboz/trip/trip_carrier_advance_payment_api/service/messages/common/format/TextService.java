package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SmsContainer;

/**
 * Интерфейс формирования текста сообщений по "Заявке на аванс"
 * {@link Advance}.
 */
public interface TextService {

    /**
     * @param advance
     * @return SmsContainer sms-message
     */
    SmsContainer createSms(Advance advance) throws MessagingException;

    /**
     * @param advance
     * @return EmailContainer email-message
     */
    EmailContainer createEmail(Advance advance) throws MessagingException;
}
