package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PersonService implements BasePersonService {
    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final ApplicationProperties props;

    private final String delim = " ";

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


    public String getAuthorFullName(Long authorId) {
        DetailedPersonInfo info = getPerson(authorId).getInfo();
        return formatFullName(info);
    }

    private String formatFullName(DetailedPersonInfo info) {
        return joinWithEmpty(Arrays.asList(info.getLastName(), info.getFirstName(), info.getMiddleName()));
    }

    private String joinWithEmpty(List<String> strings) {
        return strings.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(delim));
    }


    private BusinessLogicException getPersonInternalError(String message) {
        return ErrorUtils.getInternalError("Person-service error:" + message);
    }

}
