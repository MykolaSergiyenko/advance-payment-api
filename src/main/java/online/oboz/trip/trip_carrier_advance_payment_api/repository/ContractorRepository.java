package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    @Query(nativeQuery = true, value = " select full_Name " +
        " from common.contractors c" +
        "  left join common.payment_contractors pc " +
        "    on c.id = pc.contractor_id" +
        " where pc.id = :paymentContractorId")
    String getPaymentContractorName(@Param("paymentContractorId") Long paymentContractorId);
}
