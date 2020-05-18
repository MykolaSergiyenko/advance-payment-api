package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms;

import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessagingException;

/**
 * Интерфейс сервиса отправки смс
 */
public interface SmsSender {
    /**
     * @param sms Отправить СМС
     */
    void sendSms(SmsContainer sms) throws MessagingException;
}
