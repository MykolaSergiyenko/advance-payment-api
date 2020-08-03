package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Общие поля трипа и аванса, поэтому вынесены в структуру
 *
 * @author Ⓐbo3
 */
@Embeddable
public class TripFields implements Serializable {


    /**
     * Trip's id
     */
    @Column(name = "trip_id")
    private Long tripId;


    /**
     * Trip's num
     */
    @Column(name = "num")
    private String num;

    /**
     * Trip's driver's id
     */
    @Column(name = "driver_id")
    private Long driverId;

    /**
     * Trip's order's id
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Trip's payment contractor id
     */
    @Column(name = "payment_contractor_id")
    private Long paymentContractorId;

    /**
     * Trip's type code ('motor' only)
     */
    @Column(name = "trip_type_code", updatable = false, insertable = false)
    private String tripTypeCode = "motor";


    public TripFields() {
    }


    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
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
            ", num='" + num + '\'' +
            ", driverId=" + driverId +
            ", orderId=" + orderId +
            ", paymentContractorId=" + paymentContractorId +
            ", tripTypeCode='" + tripTypeCode + '\'' +
            '}';
    }
}
