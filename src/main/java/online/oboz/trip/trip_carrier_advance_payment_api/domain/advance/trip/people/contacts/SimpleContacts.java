package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Embeddable
@MappedSuperclass
public class SimpleContacts implements Serializable {
    final static Logger log = LoggerFactory.getLogger(SimpleContacts.class);

    @Column(name = "email")
    private String email;


    @Column(name = "phone")
    private String phone;

    public SimpleContacts() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
        return "SimpleContacts{" +
            "email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            '}';
    }

    //@Pattern(regexp = "9\\d{9}", message = "Неверный формат номера телефона.")
}
