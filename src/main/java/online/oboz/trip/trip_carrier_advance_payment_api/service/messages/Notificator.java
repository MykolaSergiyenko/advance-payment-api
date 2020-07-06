package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;

/**
 * <p>
 * <b>Интерфейс сервиса уведомлений</b>
 * <p>
 * В общем случае "Уведомления об авансе" выполняются двумя способами:
 * <p>
 * • обычное сообщение {@link Notificator#notify(Advance advance)}
 * - в момент создания "Заявки на аванс";
 * <p>
 * • отложенное сообщение {@link Notificator#scheduledNotify(Advance advance)} (online.oboz.trip.trip_carrier_advance_payment_api.domain.base.TripAdvance)} - вызывается по расписанию для "Заявок на аванс",
 * у которых есть признак непрочитанных уведомлений ("СМС с задержкой").
 * <p>
 *
 * <p>
 * В обоих случаях есть возможноть отправки уведомлений по двум каналам один за другим:
 * <p>
 * • E-mail;
 * <p>
 * • SMS.
 * <p>
 * <p>
 * Конфигурация уведомления определяется в {@link online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties}:
 * <p>
 * • Для "обычных" уведомлений - emailEnable и smsEnable.
 * <p>
 * • Для "отложенных" уведомлений - emailScheduleEnable и smsScheduleEnable.
 * <p>
 *
 * <p>
 * В текущей конфигурации разрешены только "моментальные" и-мейлы и "отложенные" смс:
 * <p>
 * • emailEnable {@link ApplicationProperties#isEmailEnabled()} = true,
 * <p>
 * • smsEnable {@link ApplicationProperties#isSmsEnabled()}= false,
 * <p>
 * • emailScheduleEnable {@link ApplicationProperties#isEmailScheduleEnabled()} = false,
 * <p>
 * • smsScheduleEnable {@link ApplicationProperties#isSmsScheduleEnabled()} = true.
 * <p>
 * <p>
 * Выборка "Заявок на аванс" для "уведомлений с задержкой" выполняется по расписанию (раз в 30 минут)
 * выполняется для "новых, непрочитанных" "Заявок" в заданном  в {@link ApplicationProperties#getSmsInterval()} интервале.
 * <p>
 * <p>
 * Например:
 * <p>
 * • smsInterval (1 час) - выполнить "отложенное" уведомление по СМС для заявок,
 * созданных не менее 1 часа назад, по которым было отправлен и-мейл,
 * но переход по ссылке на "Личный кабинет" из письма не выполнялся.
 * <p>
 * <p>
 * • emailInterval (например, 3 часа, сейчас не используется) -  выполнить "отложенное" уведомление по и-мейлу
 * для заявок, созданных не менее 3 часов назад,
 * по которым уже был отправлен и-мейл, отправлена смс,
 * но переход по ссылке на "Личный кабинет" из письма не выполнялся -
 * в теории можно использоваться для отправки еще одного и-мейла.
 * <p>
 * <p>
 * Форма самих уведомлений целиком определяется параметрами "Аванса" и шаблонами из {@link ApplicationProperties}.
 * <p>
 * Форма ссылки (длинная\сокращенная) для типов сообщений конфигурируется в {@link ApplicationProperties} :
 * <p>
 * {@link ApplicationProperties#isEmailCutLinks()} и {@link ApplicationProperties#isSmsCutLinks()} соответственно.
 * <p>
 * Сейчас emailCutLinks = true, smsCutLinks = false - короткие ссылки только для СМС,
 * но можно сконфигурирувать по-другому.
 * <p>
 *
 * @author s‡udent
 * @see online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties
 */
public interface Notificator {
    /**
     * @param advance "Request for Advance"
     *                Notificate contractor for advance by "simple" (at-moment) messages:
     *                by sms and e-mail if both pathes enable in {@link ApplicationProperties}.
     */
    Advance notify(Advance advance);


    /**
     * @param advance "Request for Advance"
     *                Notificate contractor for advance by "delayed" (scheduled) messages:
     *                by sms and e-mail if both pathes enable in {@link ApplicationProperties}.
     */
    Advance scheduledNotify(Advance advance);

}
