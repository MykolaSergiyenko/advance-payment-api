package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdvanceContactsBookRepository extends JpaRepository<AdvanceContactsBook, Long> {
    @Query("select  pc " +
        " from AdvanceContactsBook pc " +
        " where pc.contractorId = :contractor_id ")
    Optional<AdvanceContactsBook> findByContractorId(@Param("contractor_id") Long contractorId);



//    @Query(" select cb from AdvanceContactsBook cb "+
//           " inner join Trip t on cb.contractorId = t.contractorId "+
//           " and cb.isAutoAdvance <> true "+
//           " group by (cb.contractorId) "+
//           " having count (cb.contractorId) >= :min_count")
//    List<AdvanceContactsBook> findByMinTripsCount(@Param("min_count") long minCount);

//
//    @Query(nativeQuery = true, value =
//        " select c.id, c.uuid, c.contractor_id, c.full_name," +
//            " c.email, c.phone, c.is_vat_payer, c.is_auto_advance, " +
//            " c.block_auto_advance, c.created_at, c.updated_at " +
//            " from common.contractor_advance_payment_contact c " +
//            " where c.contractor_id in " +
//        "    (select cont.contractor_id " +
//        "    from common.contractor_advance_payment_contact cont " +
//        "             inner join orders.trips t " +
//        "                        on cont.contractor_id = t.contractor_id " +
//        "      and cont.is_auto_advance != true " +
//        "    group by t.contractor_id " +
//        "    having count (t.contractor_id) >= :x ) ")
//    List<AdvanceContactsBook> findByMinTripsCount(@Param("x") int minCountTrip);

//    @Query(" select contact " +
//            " from AdvanceContactsBook contact " +
//            " where exists ( " +
//            " select contr.id from AdvanceContractor contr " +
//            "   inner join Trip tr " +
//            "   on (tr.contractorId = contr.id) " +
//            "   group by (tr.contractorId) having count (tr.contractorId) >= :x) "+
//            " and contact.isAutoAdvance <> true ")
//    List<AdvanceContactsBook> findByMinTripsCount(@Param("x") int minCountTrip);
}
