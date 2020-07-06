package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(schema = "common", name = "persons")
public class Person extends AdvancePerson implements Serializable {

    final static Logger log = LoggerFactory.getLogger(Person.class);


    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public Person() {
    }


    @Override
    public String toString() {
        return "Person{" +
            super.toString() +
            '}';
    }

}
