package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractorAdvancePaymentContactRepository extends JpaRepository<ContractorAdvancePaymentContact, Long> {
    @Query(nativeQuery = true, value = "select pc.fullName, pc.email, pc.phone " +
        "from common.contractor_advance_payment_contact pc " +
        "where pc.contractor_id = :contractor_id ")
    ContractorAdvancePaymentContact findContractorAdvancePaymentContact(@Param("contractor_id") Long contractor_id);

}
