package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.trip.BaseTrip;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


//@DiscriminatorValue("Trip")
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)


@Entity
@Table(schema = "orders", name = "trips")
public class Trip extends BaseTrip { //extends TripFields
    final static Logger log = LoggerFactory.getLogger(Trip.class);
//
//    @AttributeOverrides({
//        @AttributeOverride(name="tripId",
//            column=@Column(name="id", updatable = false, insertable = false)),
//        @AttributeOverride(name="comment",
//            column=@Column(name="comment")),
//        @AttributeOverride(name="num",
//            column=@Column(name="num")),
//        @AttributeOverride(name="driverId",
//            column=@Column(name="driver_id")),
//        @AttributeOverride(name="orderId",
//            column=@Column(name="order_id")),
//        @AttributeOverride(name="paymentContractorId",
//            column=@Column(name="payment_contractor_id")),
//        @AttributeOverride(name="tripTypeCode",
//            column=@Column(name="trip_type_code")),
//        @AttributeOverride(name="tripStatusCode",
//            column=@Column(name="trip_status_code"))
//    })
//    @Embedded
//    TripFields tripFields;
//
//
//
//
//    @AttributeOverrides({
//        @AttributeOverride(name="cost",
//            column=@Column(name="cost")),
//        @AttributeOverride(name="vatCode",
//            column=@Column(name="vatCode"))
//    })
//    @Embedded
//    TripCostInfo tripCostInfo;


    @Column(name = "is_change_contractor_id")
    private Boolean isChangeContractor;

    @Column(name = "is_completed", columnDefinition = "boolean default false")
    private Boolean isCompleted;

    @Column(name = "payment_status", columnDefinition = "default 'prtly_peid'")
    private String paymentStatus;

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
            super.toString()+
            "isChangeContractor=" + isChangeContractor +
            ", isCompleted=" + isCompleted +
            ", paymentStatus='" + paymentStatus + '\'' +
            ", isFault=" + isFault +
            '}';
    }
}
