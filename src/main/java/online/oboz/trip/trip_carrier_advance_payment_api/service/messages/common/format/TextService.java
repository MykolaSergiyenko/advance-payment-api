package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SmsContainer;

/**
 * Интерфейс формирования текста сообщений по "Заявке на аванс"
 *  {@link Advance}.
 */
public interface TextService {

    /**

     */
    SmsContainer createSms(Advance advance) throws MessagingException;

    /**
     * @param TripAdvance advance
     * @return EmailContainer email-message
     */
    EmailContainer createEmail(Advance advance) throws MessagingException;

    /** Notificate all advance user's.
     * @param advance
     * @param contacts
     * @return EmailContainer email-message
     */
//    List<EmailContainer> createEmailsForAllAdvanceUsers(TripAdvance advance, List<ContactableMan> contacts) throws MessagingException;
//
//    List<SmsContainer> createSmsForAllAdvanceUsers(TripAdvance advance, List<ContactableMan> contacts) throws MessagingException;
//
//
//    List<EmailContainer> createEmailForAdvanceAuthor(TripAdvance advance, List<ContactableMan> contacts) throws MessagingException;
//    List<EmailContainer> createSmsForAdvanceAuthor(TripAdvance advance, List<ContactableMan> contacts) throws MessagingException;
//    List<EmailContainer> createSmsForAdvanceAuthor(TripAdvance advance, List<ContactableMan> contacts) throws MessagingException;
}
