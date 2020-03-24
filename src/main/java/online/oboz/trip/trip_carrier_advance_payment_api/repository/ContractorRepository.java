package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    @Query(nativeQuery = true, value = " select full_Name " +
        " from common.contractors c" +
        "  left join common.payment_contractors pc " +
        "    on c.id = pc.contractor_id" +
        " where pc.id = :paymentContractorId")
    String getFullNameByPaymentContractorId(@Param("paymentContractorId") Long paymentContractorId);

    @Query(nativeQuery = true, value = "select id, " +
        "       full_name, " +
        "       is_vat_payer, " +
        "       is_verified, " +
        "       phone, " +
        "       email, " +
        "       is_auto_advance_payment   " +
        " from common.contractors cc " +
        " where cc.id in ( " +
        "    select c.id " +
        "    from common.contractors c " +
        "             inner join orders.trips t " +
        "                        on c.id = t.contractor_id " +
        "    where t.created_at > :a " +
        "      and c.is_auto_advance_payment != true " +
        "      is_verified = true" +
        "    group by c.id " +
        "    having count(t.id) >= :x)")
    List<Contractor> getFullNameByPaymentContractorId(@Param("x") int minCountTrip, @Param("a") OffsetDateTime minDateTrip);
}
