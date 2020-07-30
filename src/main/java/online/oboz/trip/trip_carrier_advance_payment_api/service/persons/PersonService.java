package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
public class PersonService implements BasePersonService {
    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;

    private final String delim;
    private final Long advanceUserId;

    @Autowired
    public PersonService(
        PersonRepository personRepository,
        @Value("${services.auto-advance-service.auto-author}") Long autoAuthor
    ) {
        this.personRepository = personRepository;
        this.delim = " ";
        this.advanceUserId = autoAuthor;
    }

    @Override
    public Person getAdvanceSystemUser() {
        return getPerson(advanceUserId);
    }

    @Override
    public Person getPerson(Long id) {
        return personRepository.findById(id).
            orElseThrow(() -> getPersonInternalError("Author of advance not found."));
    }

    public String getAuthorFullName(Long authorId) {
        DetailedPersonInfo info = getPerson(authorId).getInfo();
        return formatFullName(info);
    }

    private String formatFullName(DetailedPersonInfo info) {
        return StringUtils.joinWithEmpty(delim,
            Arrays.asList(info.getLastName(), info.getFirstName(), info.getMiddleName()));
    }

    private BusinessLogicException getPersonInternalError(String message) {
        return ErrorUtils.getInternalError("Person-service error:" + message);
    }

}
