package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts.HasContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * Абстрактная сущность для пользователей ОБОЗ
 *
 * @author Ⓐbo3
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AdvancePerson extends HasContractor {

    /**
     * Personal info for OBOZ-users
     */
    @AttributeOverrides({
        @AttributeOverride(name = "phone", column = @Column(name = "phone")),
        @AttributeOverride(name = "email", column = @Column(name = "email")),
        @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
        @AttributeOverride(name = "middleName", column = @Column(name = "middle_name")),
        @AttributeOverride(name = "lastName", column = @Column(name = "last_name"))
    })
    @Embedded
    private DetailedPersonInfo info;


    public AdvancePerson() {

    }

    public DetailedPersonInfo getInfo() {
        return info;
    }

    public void setInfo(DetailedPersonInfo info) {
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
        return "AdvancePerson{ personInfo=" + info +"}";
    }
}
