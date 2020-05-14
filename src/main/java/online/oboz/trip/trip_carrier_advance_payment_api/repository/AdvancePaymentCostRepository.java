package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.AdvancePaymentCostDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AdvancePaymentCostRepository extends JpaRepository<AdvancePaymentCostDict, Long> {
    @Query(nativeQuery = true, value = " select c.id, advance_payment_sum,  " +
        "       max_value,  " +
        "       min_value,  " +
        "       registration_fee,  " +
        "       created_at " +
        "from dictionary.advance_payment_cost c " +
        "where c.min_value <= :cost " +
        "  and :cost <= c.max_value")
    AdvancePaymentCostDict getAdvancePaymentCost(@Param("cost") Double cost);


}
