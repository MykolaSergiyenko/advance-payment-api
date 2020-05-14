package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


//Dont'use
public class MessageDto {
    private String tripNum;
    private String contractorName;
    private String email;
    private Double advancePaymentSum;
    private String phone;
    private String lKLink;

    public MessageDto() {
    }

    public String getTripNum() {
        return this.tripNum;
    }

    public String getContractorName() {
        return this.contractorName;
    }

    public String getEmail() {
        return this.email;
    }

    public Double getAdvancePaymentSum() {
        return this.advancePaymentSum;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getLKLink() {
        return this.lKLink;
    }

    public MessageDto setTripNum(String tripNum) {
        this.tripNum = tripNum;
        return this;
    }

    public MessageDto setContractorName(String contractorName) {
        this.contractorName = contractorName;
        return this;
    }

    public MessageDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public MessageDto setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
        return this;
    }

    public MessageDto setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public MessageDto setLKLink(String lKLink) {
        this.lKLink = lKLink;
        return this;
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
        return "MessageDto{" +
            "tripNum='" + tripNum + '\'' +
            ", contractorName='" + contractorName + '\'' +
            ", email='" + email + '\'' +
            ", advancePaymentSum=" + advancePaymentSum +
            ", phone='" + phone + '\'' +
            ", lKLink='" + lKLink + '\'' +
            '}';
    }
}
