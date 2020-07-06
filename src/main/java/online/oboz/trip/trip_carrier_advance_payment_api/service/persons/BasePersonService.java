package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;

public interface BasePersonService {
    Person getAdvanceSystemUser();

    Person getPerson(Long id);

    IsTripAdvanced setAuthorInfo(IsTripAdvanced page, Long authorId);
}
