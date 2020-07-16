package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.validation.constraints.Pattern;

public class SendSmsRequest {
    /**
     * Text of message
     */
    @JsonProperty("text")
    private String text;


    /**
     * Phone number
     */
    @Pattern(regexp = "7\\d{7}", message = "Неверный номер телефона.")
    @JsonProperty("phone")
    private String phone;


    public SendSmsRequest(String text, String phone) {
        this.text = text;
        this.phone = phone;
    }

    public SendSmsRequest() {
    }

    public String getText() {
        return this.text;
    }

    public String getPhone() {
        return this.phone;
    }


    public void setText(String text) {
        this.text = text;
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
        return "SmsContainer { text=" + text + ", phone='" + phone + "}";
    }
}
