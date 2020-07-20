package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractorRepository extends JpaRepository<AdvanceContractor, Long> {

    @Query("select contractr from AdvanceContractor contractr " +
        "where contractr.isAutoContractor <> true and contractr.id in " +
        "(select contact.contractorId from AdvanceContactsBook contact " +
        "inner join Advance adv on (adv.contractorId = contact.contractorId " +
        "and adv.isPaid = true and adv.isCancelled = false) " +
        "group by (contact.contractorId) having count (adv.contractorId) >= :x)")
    List<AdvanceContractor> findByMinCountAdvancesPaid(@Param("x") long minCount);
}
