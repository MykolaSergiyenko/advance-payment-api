package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class PersonService implements BasePersonService {
    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final ApplicationProperties props;


    public PersonService(PersonRepository personRepository, ApplicationProperties props) {
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
        HttpStatus status = INTERNAL_SERVER_ERROR;
        Error error = getServiceError(status, message);
        return getInternalBusinessError(error, status);
    }

    private Error getServiceError(HttpStatus state, String message) {
        Error error = new Error().status(state.toString());
        error.errorCode(((Integer) state.value()).toString());
        error.setErrorMessage("PersonsService - Business Error: " + message);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }
}
