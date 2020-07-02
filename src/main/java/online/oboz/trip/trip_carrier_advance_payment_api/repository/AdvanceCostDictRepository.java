package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface AdvanceCostDictRepository extends JpaRepository<AdvanceCostDict, Long> {
    @Query(nativeQuery = true, value = " select dict"+
        "from AdvanceCostDict dict " +
        "where :cost between dict.min_value and dict.max_value")
    Optional<AdvanceCostDict> getAdvancePaymentCost(@Param("cost") Double cost);
}
