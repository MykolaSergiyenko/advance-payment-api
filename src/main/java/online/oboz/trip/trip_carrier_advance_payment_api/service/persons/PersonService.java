package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;


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

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person getAdvanceSystemUser() {
        return personRepository.findById(47700l).orElse(null);

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
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }

    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("PersonsService - Business Error: " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }
}
