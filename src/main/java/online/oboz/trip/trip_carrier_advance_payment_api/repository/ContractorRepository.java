package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractorRepository extends JpaRepository<AdvanceContractor, Long> {
//    @Query(nativeQuery = true, value = " select full_Name " +
//        " from common.contractors c" +
//        "  left join common.payment_contractors pc " +
//        "    on c.id = pc.contractor_id" +
//        " where pc.id = :paymentContractorId")
//    String getPaymentContractorName(@Param("paymentContractorId") Long paymentContractorId);
//
//    @Query(nativeQuery = true, value = "select id, " +
//        "       full_name, " +
//        "       is_vat_payer, " +
//        "       is_verified, " +
//        "       phone, " +
//        "       email, " +
//        "       is_auto_advance_payment   " +
//        " from common.contractors cc " +
//        " where cc.id in ( " +
//        "    select c.id " +
//        "    from common.contractors c " +
//        "             inner join orders.trips t " +
//        "                        on c.id = t.contractor_id " +
//        "      and c.is_auto_advance_payment != true " +
//        "      and is_verified = true" +
//        "    group by c.id " +
//        "    having count(t.id) >= :x)")
//    List<TripAdvanceContractor> getContractorByTripCount(@Param("x") int minCountTrip, @Param("a") OffsetDateTime minDateTrip);

    //" and contr.isAutoContractor <> true ) ")
    @Query("select contractr from AdvanceContractor contractr " +
        "where contractr.isAutoContractor <> true and contractr.id in (" +
        "select contact.contractorId from AdvanceContactsBook contact " +
        "inner join Advance adv on (adv.contractorId = contact.contractorId " +
        "and adv.isPaid = true and adv.isCancelled = false) " +
        "group by (contact.contractorId) " +
        "having count (adv.contractorId) >= :x)")
    List<AdvanceContractor> findByMinCountAdvancesPaid(@Param("x") long minCount);
}
