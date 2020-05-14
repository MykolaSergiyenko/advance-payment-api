package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter;

import java.net.URL;

/**
 * ИНтерфейс сервисов для "конвертации" ссылок
 * Принимает ссылку, возвращает преобразованную ссылку в строковом представлении.
 */
public interface UrlService {
    String editUrl(URL url);

    String editUrl(String url);
}
