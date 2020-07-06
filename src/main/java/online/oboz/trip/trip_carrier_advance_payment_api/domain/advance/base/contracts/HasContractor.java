package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUuidEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;


@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class HasContractor extends BaseUuidEntity {

    @Column(name = "contractor_id")
    private Long contractorId;


    public HasContractor() {
    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
    }


    public Long getContractorId() {
        return contractorId;
    }


    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
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
        return "Has_Contractor {" +
            "contractorId=" + contractorId +
            '}';
    }
}
