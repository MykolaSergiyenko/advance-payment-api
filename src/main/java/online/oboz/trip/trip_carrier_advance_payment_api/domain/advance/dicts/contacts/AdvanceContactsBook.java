package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


/**
 * Контакты авансирования - вкладка в карточке "Контрагента"
 *
 * @author Ⓐbo3
 */
@Entity
@Table(name = "contractor_advance_payment_contact", schema = "common")
public class AdvanceContactsBook extends AdvanceContact {

    // TODO: add sequence generator?
    //@SequenceGenerator(name = "default_gen", sequenceName = "trip_request_advance_payment_id_seq", allocationSize = 1, schema = "orders")
    public AdvanceContactsBook() {
    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
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
