package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripFields;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info.CommonLocation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Set;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class TripsAdvance extends AuthorsAdvance {
    final static Logger log = LoggerFactory.getLogger(TripsAdvance.class);




    @AttributeOverrides({
        @AttributeOverride(name="tripId", column=@Column(name="trip_id")),
        @AttributeOverride(name="num", column=@Column(name="trip_num")),
        @AttributeOverride(name="comment", column=@Column(name="comment")),
        @AttributeOverride(name="driverId", column=@Column(name="driver_id")),
        @AttributeOverride(name="orderId",  column=@Column(name="order_id")),
        @AttributeOverride(name="paymentContractorId", column=@Column(name="payment_contractor_id")),
        @AttributeOverride(name="tripTypeCode", column=@Column(name="trip_type_code")),
        @AttributeOverride(name="tripStatusCode", column=@Column(name="trip_status_code"))
    })
    @Embedded
    private TripFields advanceTripFields;




    @AttributeOverrides({
        @AttributeOverride(name="cost", column=@Column(name="trip_cost")),
        @AttributeOverride(name="vatCode", column=@Column(name="trip_vat_code")),
        @AttributeOverride(name="ndsCost",  column=@Column(name="nds_cost"))
    })
    @Embedded
    private TripCostInfo costInfo;


    @AttributeOverrides({
        @AttributeOverride(name="advancePaymentSum", column=@Column(name="advance_payment_sum")),
        @AttributeOverride(name="registrationFee", column=@Column(name="registration_fee"))
    })
    @Embedded
    private AdvanceInfo tripAdvanceInfo;


//    @ElementCollection(targetClass= CommonLocation.class)
//    @Formula(value = " case when trip_id > 0 then " +
//        " (select loc from common.locations loc " +
//        "   inner join orders.trips tr on tr.id = trip_id " +
//        "   inner join orders.trip_info trinfo on " +
//        "       (trinfo.id = tr.id " +
//        "               and loc.location_id in " +
//        "                   (trinfo.origin_place_id, " +
//        "                   trinfo.destination_place_id))) " +
//        " end ")
//    private Set<CommonLocation> locations;



    @Formula(value = " case when trip_id > 0 then " +
        " (select loc from common.locations loc " +
        "   inner join orders.trips tr on tr.id = trip_id " +
        "   inner join orders.trip_info trinfo on " +
        "       (trinfo.trip_id = tr.id " +
        "               and loc.location_id = trinfo.origin_place_id)) " +
        " end ")
    private CommonLocation loadingLocation;

    @Formula(value = " case when trip_id > 0 then " +
        " (select loc from common.locations loc " +
        "   inner join orders.trips tr on tr.id = trip_id " +
        "   inner join orders.trip_info trinfo on " +
        "       (trinfo.trip_id = tr.id " +
        "               and loc.location_id = trinfo.destination_place_id)) " +
        " end ")
    private CommonLocation unloadingLocation;


    @Formula(value = " case when trip_id > 0 then " +
        " (select trinfo.origin_name from orders.trip_info trinfo" +
        "  where trinfo.trip_id = trip_id) end ")
    private String loadingAddress;

    @Formula(value = " case when trip_id > 0 then " +
        " (select trinfo.destination_name from orders.trip_info trinfo on " +
        "  where trinfo.trip_id = trip_id) end ")
    private String unloadingAddress;

    public TripsAdvance() {
    }

//    public Set<CommonLocation> getLocations() {
//        return locations;
//    }
//
//    public void setLocations(Set<CommonLocation> locations) {
//        this.locations = locations;
//    }





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
//
//
//    public Long getTripId() {
//        return tripId;
//    }
//
//    public void setTripId(Long tripId) {
//        this.tripId = tripId;
//    }

    public TripFields getAdvanceTripFields() {
        return advanceTripFields;
    }

    public void setAdvanceTripFields(TripFields advanceTripFields) {
        this.advanceTripFields = advanceTripFields;
    }

    @Transient
    public CommonLocation getLoadingLocation() {
        return loadingLocation;
    }

    public void setLoadingLocation(CommonLocation loadingLocation) {
        this.loadingLocation = loadingLocation;
    }

    @Transient
    public CommonLocation getUnloadingLocation() {
        return unloadingLocation;
    }

    public void setUnloadingLocation(CommonLocation unloadingLocation) {
        this.unloadingLocation = unloadingLocation;
    }

    public String getLoadingAddress() {
        return loadingAddress;
    }

    public void setLoadingAddress(String loadingAddress) {
        this.loadingAddress = loadingAddress;
    }

    public String getUnloadingAddress() {
        return unloadingAddress;
    }

    public void setUnloadingAddress(String unloadingAddress) {
        this.unloadingAddress = unloadingAddress;
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
//            "tripId=" + tripId +
            ", advanceTripFields=" + advanceTripFields +
            ", costInfo=" + costInfo +
            ", tripAdvanceInfo=" + tripAdvanceInfo +
//            ", locations=" + locations +
            ", locations=" + loadingLocation +
            ", locations=" + unloadingLocation +
            '}';
    }
}
