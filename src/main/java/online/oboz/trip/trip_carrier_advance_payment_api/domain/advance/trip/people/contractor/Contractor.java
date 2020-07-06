package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUuidEntity;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Contractor extends BaseUuidEntity {
    final static Logger log = LoggerFactory.getLogger(Contractor.class);


    @Column(name = "is_auto_advance_payment")
    private Boolean isAutoContractor;

    @Column(name = "kind")
    private String kind;

    @Column(name = "is_vat_payer")
    private Boolean isVatPayer;


    @AttributeOverrides({
        @AttributeOverride(name = "fullName", column = @Column(name = "full_name",
            insertable = false, updatable = false)),
        @AttributeOverride(name = "phone", column = @Column(name = "phone")),
        @AttributeOverride(name = "email", column = @Column(name = "email"))
    })
    @Embedded
    private FullNamePersonInfo contractorContacts;


    @Column(name = "full_name")
    private String fullName;
//
//    @Column(name = "phone")
//    private String phone;
//
//    @Column(name = "email")
//    private String email;


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }


    public Boolean getVatPayer() {
        return isVatPayer;
    }

    public void setVatPayer(Boolean vatPayer) {
        isVatPayer = vatPayer;
    }


    public Boolean getAutoContractor() {
        return isAutoContractor;
    }

    public void setAutoContractor(Boolean autoContractor) {
        isAutoContractor = autoContractor;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public FullNamePersonInfo getContractorContacts() {
        return contractorContacts;
    }

    public void setContractorContacts(FullNamePersonInfo contractorContacts) {
        this.contractorContacts = contractorContacts;
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
        return "Contractor{" +
            super.toString() +
            "isAutoContractor=" + isAutoContractor +
            ", kind='" + kind + '\'' +
            ", isVatPayer=" + isVatPayer +
            ", contractorContacts=" + contractorContacts +
            '}';
    }
}
