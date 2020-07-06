package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(schema = "common", name = "contractors")
public class AdvanceContractor extends Contractor {
    final static Logger log = LoggerFactory.getLogger(AdvanceContractor.class);


    public AdvanceContractor() {
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
        return "AdvanceContractor{}";
    }
}
