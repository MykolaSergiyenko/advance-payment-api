package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TripPointDto {

    @JsonProperty
    private Integer tripId;
    @JsonProperty
    private String address;
    @JsonProperty
    private String additionalServiceCode;
    @JsonProperty
    private String num;

    public TripPointDto() {
    }

    public Integer getTripId() {
        return this.tripId;
    }

    public String getAddress() {
        return this.address;
    }

    public String getAdditionalServiceCode() {
        return this.additionalServiceCode;
    }

    public String getNum() {
        return this.num;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAdditionalServiceCode(String additionalServiceCode) {
        this.additionalServiceCode = additionalServiceCode;
    }

    public void setNum(String num) {
        this.num = num;
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
        return "TripPointDto(tripId=" +
            this.getTripId() + ", address=" +
            this.getAddress() + ", additionalServiceCode=" +
            this.getAdditionalServiceCode() + ", num=" +
            this.getNum() + ")";
    }
}
