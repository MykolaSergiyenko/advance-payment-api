package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripFields;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * "Аванс для Трипа"
 * - аванс проецирует на себя много полей из родительского Трипа
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class TripsAdvance extends AuthorsAdvance {
    final static Logger log = LoggerFactory.getLogger(TripsAdvance.class);


    /**
     * Trip fields projected to Advance
     */
    @AttributeOverrides({
        @AttributeOverride(name = "tripId", column = @Column(name = "trip_id")),
        @AttributeOverride(name = "num", column = @Column(name = "trip_num")),
        @AttributeOverride(name = "driverId", column = @Column(name = "driver_id")),
        @AttributeOverride(name = "orderId", column = @Column(name = "order_id")),
        @AttributeOverride(name = "paymentContractorId", column = @Column(name = "payment_contractor_id")),
        @AttributeOverride(name = "tripTypeCode", column = @Column(name = "trip_type_code"))
    })
    @Embedded
    private TripFields advanceTripFields;


    /**
     * Trip cost-fields projected to Advance
     */
    @AttributeOverrides({
        @AttributeOverride(name = "cost", column = @Column(name = "trip_cost"))
    })
    @Embedded
    private TripCostInfo costInfo;


    /**
     * Advance costs calculated by Trip params and dictionaries
     */
    @AttributeOverrides({
        @AttributeOverride(name = "advancePaymentSum", column = @Column(name = "advance_payment_sum")),
        @AttributeOverride(name = "registrationFee", column = @Column(name = "registration_fee"))
    })
    @Embedded
    private AdvanceInfo tripAdvanceInfo;


    public TripsAdvance() {
    }

    public AdvanceInfo getTripAdvanceInfo() {
        return tripAdvanceInfo;
    }

    public void setTripAdvanceInfo(AdvanceInfo tripAdvanceInfo) {
        this.tripAdvanceInfo = tripAdvanceInfo;
    }


    public TripCostInfo getCostInfo() {
        return costInfo;
    }

    public void setCostInfo(TripCostInfo tripCostInfo) {
        this.costInfo = tripCostInfo;
    }


    public TripFields getAdvanceTripFields() {
        return advanceTripFields;
    }

    public void setAdvanceTripFields(TripFields advanceTripFields) {
        this.advanceTripFields = advanceTripFields;
    }


    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
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
        return "TripsAdvance{" +
            ", advanceTripFields=" + advanceTripFields +
            ", costInfo=" + costInfo +
            ", tripAdvanceInfo=" + tripAdvanceInfo +
            '}';
    }
}
