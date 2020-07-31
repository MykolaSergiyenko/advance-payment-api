package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUpdateEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * Платежный контрагент
 *
 * @author Ⓐbo3
 */
@Entity
@Table(schema = "common", name = "payment_contractors")
public class TripPaymentContractor extends BaseUpdateEntity {


    /**
     * Id контрагента
     */
    @Column(name = "contractor_id", insertable = false, updatable = false)
    private Long contractorId;

    /**
     * Contractor
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_id")
    private AdvanceContractor contractor;


    public TripPaymentContractor() {
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public AdvanceContractor getContractor() {
        return contractor;
    }

    public void setContractor(AdvanceContractor contractor) {
        this.contractor = contractor;
    }


    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    @Override
    public String toString() {
        return "TripPaymentContractor{" +
            "contractorId=" + contractorId +
            ", contractor=" + contractor +
            '}';
    }
}
