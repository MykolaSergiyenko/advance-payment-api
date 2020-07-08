package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PersonService implements BasePersonService {
    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final ApplicationProperties props;

    @Autowired
    public PersonService(
        PersonRepository personRepository,
        ApplicationProperties props
    ) {
        this.personRepository = personRepository;
        this.props = props;
    }

    @Override
    public Person getAdvanceSystemUser() {
        return getPerson(props.getAutoAuthor());
    }

    @Override
    public Person getPerson(Long id) {
        return personRepository.findById(id).
            orElseThrow(() -> getPersonInternalError("Author of advance not found."));
    }

    @Override
    public IsTripAdvanced setAuthorInfo(IsTripAdvanced page, Long authorId) {
        Person author = getPerson(authorId);
        page.setFirstName(author.getInfo().getFirstName());
        page.setLastName(author.getInfo().getLastName());
        page.setMiddleName(author.getInfo().getMiddleName());
        page.setAuthorId(authorId);
        return page;
    }


    private BusinessLogicException getPersonInternalError(String message) {
        return ErrorUtils.getInternalError("Person-service error:" + message);
    }

}
