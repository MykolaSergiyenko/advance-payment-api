package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts.HasContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;


import javax.persistence.*;

/**
 * Контакт авансирвоания
 *
 * @author Ⓐbo3
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AdvanceContact extends HasContractor {

    @AttributeOverrides({
        @AttributeOverride(name = "fullName", column = @Column(name = "full_name"))
    })
    @Embedded
    private FullNamePersonInfo info;

    public AdvanceContact() {
    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public FullNamePersonInfo getInfo() {
        return info;
    }

    public void setInfo(FullNamePersonInfo info) {
        this.info = info;
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
        return "AdvanceContact{ info=" + info + "}";
    }
}
