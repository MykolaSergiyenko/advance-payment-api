package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.validation.constraints.Pattern;

public class SmsContainer {
    @JsonProperty("text")
    private String text;


    @Pattern(regexp = "7\\d{7}", message = "Неверный номер телефона.")
    @JsonProperty("phone")
    private String phone;

    @JsonIgnore
    private String tripNum;

    public SmsContainer(String text, String phone, String tripNum) {
        this.text = text;
        this.phone = phone;
        this.tripNum = tripNum;
    }

    public SmsContainer() {
    }

    public String getText() {
        return this.text;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getTripNum() {
        return this.tripNum;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTripNum(String tripNum) {
        this.tripNum = tripNum;
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
        return "SmsContainer {" +
            " text='" + text + '\'' +
            ", phone='" + phone + '\'' +
            ", tripNum='" + tripNum + '\'' +
            '}';
    }
}
