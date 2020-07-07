package online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats;

import java.util.List;

public interface VatService {
    Double getVatValue(String vatCode);

    List<String> getZeroCodes();
}
