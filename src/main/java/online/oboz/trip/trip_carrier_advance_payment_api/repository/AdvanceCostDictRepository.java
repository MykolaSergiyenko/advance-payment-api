package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface AdvanceCostDictRepository extends JpaRepository<AdvanceCostDict, Long> {

    @Query("select cdict from AdvanceCostDict cdict where :cost between cdict.minValue and cdict.maxValue")
    Optional<AdvanceCostDict> getAdvancePaymentCost(@Param("cost") Double cost);

    @Query("select min (cdict.minValue) from AdvanceCostDict cdict")
    Double getMinCost();

    @Query("select max (cdict.maxValue) from AdvanceCostDict cdict")
    Double getMaxCost();
}
