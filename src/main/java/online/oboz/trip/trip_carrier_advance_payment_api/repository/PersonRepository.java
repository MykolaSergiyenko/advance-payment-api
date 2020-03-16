package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PersonRepository extends JpaRepository<Person, Long> {

}
