package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsContainer;

/**
 * Интерфейс формирования текста сообщений по "Заявке на аванс"
 *  {@link TripAdvance}.
 */
public interface TextService {

    /**
     * @param advance
     * @return SmsContainer sms-message
     */
    SmsContainer createSms(TripAdvance advance) throws MessagingException;

    /**
     * @param advance
     * @return EmailContainer email-message
     */
    EmailContainer createEmail(TripAdvance advance) throws MessagingException;
}
