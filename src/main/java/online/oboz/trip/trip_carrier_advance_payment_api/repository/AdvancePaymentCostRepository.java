package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.AdvancePaymentCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AdvancePaymentCostRepository extends JpaRepository<AdvancePaymentCost, Long> {
    @Query(nativeQuery = true, value = " select c from dictionary.advance_payment_cost c where c.min_value <= :cost and :cost<=c.max_value")
    AdvancePaymentCost searchAdvancePaymentCost(@Param("cost")BigDecimal cost);


}
