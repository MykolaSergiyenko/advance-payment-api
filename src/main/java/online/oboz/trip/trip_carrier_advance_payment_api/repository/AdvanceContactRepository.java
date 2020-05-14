package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.AdvanceContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdvanceContactRepository extends JpaRepository<AdvanceContact, Long> {
    @Query("select  pc " +
        " from AdvanceContact pc " +
        " where pc.contractorId = :contractor_id ")
    Optional<AdvanceContact> find(@Param("contractor_id") Long contractorId);

}
