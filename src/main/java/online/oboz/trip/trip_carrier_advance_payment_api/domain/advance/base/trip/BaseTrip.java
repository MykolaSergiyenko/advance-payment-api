package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripFields;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts.HasContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseTrip extends HasContractor {
    final static Logger log = LoggerFactory.getLogger(BaseTrip.class);

    @AttributeOverrides({
        @AttributeOverride(name="tripId",
            column=@Column(name="id", updatable = false, insertable = false)),
        @AttributeOverride(name="comment",
            column=@Column(name="comment")),
        @AttributeOverride(name="num",
            column=@Column(name="num")),
        @AttributeOverride(name="driverId",
            column=@Column(name="driver_id")),
        @AttributeOverride(name="orderId",
            column=@Column(name="order_id")),
        @AttributeOverride(name="paymentContractorId",
            column=@Column(name="payment_contractor_id")),
        @AttributeOverride(name="tripTypeCode",
            column=@Column(name="trip_type_code")),
        @AttributeOverride(name="tripStatusCode",
            column=@Column(name="trip_status_code"))
    })
    @Embedded
    TripFields tripFields;




    @AttributeOverrides({
        @AttributeOverride(name="cost",
            column=@Column(name="cost")),
        @AttributeOverride(name="vatCode",
            column=@Column(name="vatCode"))
    })
    @Embedded
    TripCostInfo tripCostInfo;


    public TripFields getTripFields() {
        return tripFields;
    }

    public void setTripFields(TripFields tripFields) {
        this.tripFields = tripFields;
    }

    public TripCostInfo getTripCostInfo() {
        return tripCostInfo;
    }

    public void setTripCostInfo(TripCostInfo tripCostInfo) {
        this.tripCostInfo = tripCostInfo;
    }

    @Override
    public String toString() {
        return "BaseTrip{" +
            super.toString() +
            "tripFields=" + tripFields +
            ", tripCostInfo=" + tripCostInfo +
            '}';
    }
}
