package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.VatCostDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface VatsRepository extends JpaRepository<VatCostDict, Long> {

    @Query("select (v.value)/100 from VatCostDict v where v.code = :vatCode ")
    Double findByCode(@Param("vatCode") String vatCode);
}
