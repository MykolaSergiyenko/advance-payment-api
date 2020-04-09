package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContractorContactRepository extends JpaRepository<ContractorAdvancePaymentContact, Long> {
    @Query("select  pc " +
        " from ContractorAdvancePaymentContact pc " +
        " where pc.contractorId = :contractor_id ")
    Optional<ContractorAdvancePaymentContact> find(@Param("contractor_id") Long contractorId);

}
