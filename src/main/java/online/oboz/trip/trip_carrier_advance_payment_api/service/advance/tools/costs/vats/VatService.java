package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs.vats;

import java.util.List;

/**
 * Сервис для работы со справочником НДС
 */
public interface VatService {

    /**
     * Получить процент НДС по коду
     * @param vatCode - код НДС
     * @return Double - ставка
     */
    Double getVatValue(String vatCode);

    /**
     * Получить "нулевые" (процент НДС равен 0)
     * коды НДС
     * @return List<String> - список "нулевых" кодов
     */
    List<String> getZeroCodes();
}
