package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    //and r.contractor_id is not null

//    @Query(nativeQuery = true,
//        value = "select r.id, r.uuid, r.first_name, r.middle_name, r.last_name, " +
//            "r.contractor_id, r.phone, r.email, " +
//            "r.created_at, r.updated_at " +
//            "from common.persons r where r.email in (:a)")
//    Optional<Person> findByEmail(@Param("a") String a);
//
//    @Query("select r from Person r where (r.info.email = :email)")
//    Optional<Person> findByEmail(@Param("email") String email);
}
