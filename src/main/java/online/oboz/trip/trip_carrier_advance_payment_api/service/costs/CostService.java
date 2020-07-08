package online.oboz.trip.trip_carrier_advance_payment_api.service.costs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

public interface CostService {

    Double calculateNdsCost(Double tripCost, String vatCode, Boolean isVatPayer);

    Advance setSumsToAdvance(Advance advance, Trip trip);
}
