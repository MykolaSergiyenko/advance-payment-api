package online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor;

import java.net.URL;

/**
 * Интерфейс сервисов для "конвертации" ссылок
 * Принимает ссылку, возвращает преобразованную ссылку в строковом представлении.
 */
public interface UrlService {
    String editUrl(URL url);

    /**
     * Редактировать URL
     * @param url
     * @return URL in String
     */
    String editUrl(String url);
}
