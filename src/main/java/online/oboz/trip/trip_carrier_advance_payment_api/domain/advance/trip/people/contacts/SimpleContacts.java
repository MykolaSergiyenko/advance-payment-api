package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Embeddable
public class SimpleContacts implements Serializable {
    private String email;

    @Pattern(regexp = "9\\d{9}", message = "Неверный формат номера телефона.")
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
}
