package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvanceExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContractorAdvanceExclusionRepository extends JpaRepository<ContractorAdvanceExclusion, Long> {
    @Query("select ce from ContractorAdvanceExclusion ce where ce.carrierId =:contractorId " +
        "and ce.orderTypeId =:orderTypeId")
    Optional<ContractorAdvanceExclusion> findByContractorId(@Param("contractorId") Long contractorId, @Param("orderTypeId") Long orderTypeId);
}
