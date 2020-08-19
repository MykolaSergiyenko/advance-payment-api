package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUuidEntity;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * Контрагент
 *
 * @author Ⓐbo3
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Contractor extends BaseUuidEntity {
    private static final Logger log = LoggerFactory.getLogger(Contractor.class);

    /**
     * Auto-advance-contractor
     */
    @Column(name = "is_auto_advance_payment")
    private Boolean isAutoContractor;

    /**
     * Kind of contractors
     */
    @Column(name = "kind")
    private String kind;

    /**
     * Is contractor payer of VAT
     */
    @Column(name = "is_vat_payer")
    private Boolean isVatPayer;


    /**
     * Contractor's self personal info
     */
    @AttributeOverrides({
        @AttributeOverride(name = "fullName", column = @Column(name = "full_name",
            insertable = false, updatable = false)),
        @AttributeOverride(name = "phone", column = @Column(name = "phone")),
        @AttributeOverride(name = "email", column = @Column(name = "email"))
    })
    @Embedded
    private FullNamePersonInfo contractorContacts;


    /**
     * Full contractor's name
     */
    @Column(name = "full_name")
    private String fullName;



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
        if (autoContractor && this.isAutoContractor){
            log.info("[Контрагент]:  [{}] - '{}' - признак 'авто-аванс' уже выставлен.",
                this.getId(), this.getFullName());
            return;
        } else {
            log.info("[Контрагент]: [{}] - '{}' - выставлен признак 'авто-аванс' в значение '{}'.",
                this.getId(), this.getFullName(), autoContractor);
            isAutoContractor = autoContractor;
        }
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
