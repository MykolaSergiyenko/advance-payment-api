package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;


@Entity
@Table(name = "contractor_advance_payment_contact", schema = "common")
public class AdvanceContactsBook extends AdvanceContact {


    public AdvanceContactsBook() {
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
        return "AdvanceContactsBook{" +
            super.toString() +
            "}";
    }
}
