package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.Max;


@Embeddable
public class FullNamePersonInfo extends SimpleContacts {


    @Column(name = "full_name")
    private String fullName;

    public FullNamePersonInfo() {
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
        return "FullnamePersonInfo{" +
            "fullName=" + fullName +
            '}';
    }
}
