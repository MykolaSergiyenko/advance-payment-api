package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.hibernate.annotations.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;


//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)


@Embeddable
public class TripFields implements Serializable { //extends HasContractor
    final static Logger log = LoggerFactory.getLogger(TripFields.class);



    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "num")
    private String num;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_contractor_id")
    private Long paymentContractorId;

    @Column(name = "trip_type_code", updatable = false, insertable = false)
    private String tripTypeCode;

    @Column(name = "trip_status_code")
    private String tripStatusCode;


    public TripFields() {
    }




    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentContractorId() {
        return paymentContractorId;
    }

    public void setPaymentContractorId(Long paymentContractorId) {
        this.paymentContractorId = paymentContractorId;
    }

    public String getTripTypeCode() {
        return tripTypeCode;
    }

    public void setTripTypeCode(String tripTypeCode) {
        this.tripTypeCode = tripTypeCode;
    }

    public String getTripStatusCode() {
        return tripStatusCode;
    }

    public void setTripStatusCode(String tripStatusCode) {
        this.tripStatusCode = tripStatusCode;
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
        return "TripFields{" +
            "tripId=" + tripId +

            ", comment='" + comment + '\'' +
            ", num='" + num + '\'' +
            ", driverId=" + driverId +
            ", orderId=" + orderId +
            ", paymentContractorId=" + paymentContractorId +
            ", tripTypeCode='" + tripTypeCode + '\'' +
            ", tripStatusCode='" + tripStatusCode + '\'' +
            '}';
    }
}
