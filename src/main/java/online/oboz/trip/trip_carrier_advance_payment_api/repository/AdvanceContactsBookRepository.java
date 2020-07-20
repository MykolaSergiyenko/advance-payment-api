package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface AdvanceContactsBookRepository extends JpaRepository<AdvanceContactsBook, Long> {

    @Query("select  pc from AdvanceContactsBook pc where pc.contractorId = :contractor_id")
    Optional<AdvanceContactsBook> findByContractorId(@Param("contractor_id") Long contractorId);

}
