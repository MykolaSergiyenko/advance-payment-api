package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class DetailedPersonInfo extends SimpleContacts implements Serializable {

    @Column(name = "first_name")
    private String firstName;


    @Column(name = "last_name")
    private String lastName;


    @Column(name = "middle_name")
    private String middleName;

//    @Formula(value = "concat(first_name, last_name, middle_name)")
//    private String fullName;



    public DetailedPersonInfo() {
    }


    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
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
        return "DetailedPersonInfo{" +
            "firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", middleName='" + middleName + '\'' +
//            ", fullName='" + fullName + '\'' +
            '}';
    }
}
