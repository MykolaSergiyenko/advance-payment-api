package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SendSmsRequest {
    @JsonProperty("text")
    private String text;

    @JsonProperty("phone")
    private String phone;

    @JsonIgnore
    private String tripNum;

    public SendSmsRequest(String text, String phone, String tripNum) {
        this.text = text;
        this.phone = phone;
        this.tripNum = tripNum;
    }

    public SendSmsRequest() {
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

    public String toString() {
        return "SendSmsRequest(text=" +
            this.getText() + ", phone=" +
            this.getPhone() + ", tripNum=" +
            this.getTripNum() + ")";
    }
}
