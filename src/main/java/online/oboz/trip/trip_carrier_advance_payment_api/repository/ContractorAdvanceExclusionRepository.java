package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvanceExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ContractorAdvanceExclusionRepository extends JpaRepository<ContractorAdvanceExclusion, Long> {
    @Query(nativeQuery = true, value = " select id, " +
        "       carrier_full_name, " +
        "       carrier_id, " +
        "       is_automation_advance_payment, " +
        "       is_confirm_advance, " +
        "       order_type_id, " +
        "       created_at, " +
        "       updated_at, " +
        "       deleted_at " +
        "from common.contractor_advance_exclusion where carrier_id=:contractorId;")
    Optional<ContractorAdvanceExclusion> findByContractorId(@Param("contractorId") Long contractorId);
}
