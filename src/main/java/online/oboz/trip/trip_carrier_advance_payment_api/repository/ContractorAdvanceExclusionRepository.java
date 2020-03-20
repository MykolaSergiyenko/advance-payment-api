package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvanceExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ContractorAdvanceExclusionRepository extends JpaRepository<ContractorAdvanceExclusion, Long> {
//    TODO change Optional<ContractorAdvanceExclusion> to List<ContractorAdvanceExclusion>
@Query(" select ce from ContractorAdvanceExclusion ce where ce.carrierId =:contractor_id ")
Optional<ContractorAdvanceExclusion> findByContractorId(@Param("contractor_id") Long contractorId);
}
