package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.trip.BaseTrip;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


/**
 * Поездки
 *
 * @author Ⓐbo3
 */
@Entity
@Table(schema = "orders", name = "trips")
public class Trip extends BaseTrip {


    /**
     * Trip's comment
     */
    @Column(name = "comment")
    private String comment;

    /**
     * Trip's state
     */
    @Column(name = "trip_status_code")
    private String tripStatusCode;


    /**
     * Trip's VAT-code
     */
    @Column(name = "vat_code", columnDefinition = "default 'twenty_five'")
    private String vatCode;


    /**
     * Is contractor changed for Trip
     */
    @Column(name = "is_change_contractor_id")
    private Boolean isChangeContractor;

    /**
     * Is Trip completed
     */
    @Column(name = "is_completed", columnDefinition = "boolean default false")
    private Boolean isCompleted;

    /**
     * Trip's payment state
     */
    @Column(name = "payment_status", columnDefinition = "default 'prtly_peid'")
    private String paymentStatus;

    /**
     * Is trip fault
     */
    @Column(name = "is_fault", columnDefinition = "boolean default false")
    private Boolean isFault;

    public Trip() {
    }


    public Boolean getChangeContractor() {
        return isChangeContractor;
    }

    public void setChangeContractor(Boolean changeContractor) {
        isChangeContractor = changeContractor;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Boolean getFault() {
        return isFault;
    }

    public void setFault(Boolean fault) {
        isFault = fault;
    }

    public String getTripStatusCode() {
        return tripStatusCode;
    }

    public void setTripStatusCode(String tripStatusCode) {
        this.tripStatusCode = tripStatusCode;
    }

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
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
        return "Trip{" +
            "comment='" + comment + '\'' +
            ", tripStatusCode='" + tripStatusCode + '\'' +
            ", vatCode='" + vatCode + '\'' +
            ", isChangeContractor=" + isChangeContractor +
            ", isCompleted=" + isCompleted +
            ", paymentStatus='" + paymentStatus + '\'' +
            ", isFault=" + isFault +
            '}';
    }
}
