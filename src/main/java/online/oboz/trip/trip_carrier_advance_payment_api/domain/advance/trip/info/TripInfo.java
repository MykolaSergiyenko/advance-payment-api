package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseEntity;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Информация по поездке - содержит конечные точки и даты
 *
 * @author Ⓐbo3
 */
@Entity
@Table(schema = "orders", name = "trip_infos")
public class TripInfo extends BaseEntity {


    /**
     * Trip's id
     */
    @Column(name = "trip_id", updatable = false, insertable = false)
    private Long tripId;


    /**
     * Trip
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;


    /**
     * Start point
     */
    @Column(name = "origin_place_id", updatable = false, insertable = false)
    private String originPlaceId;


    /**
     * Start address
     */
    @Column(name = "origin_name")
    private String originName;

    /**
     * Start date
     */
    @Column(name = "start_date")
    private OffsetDateTime startDate;


    /**
     * End point
     */
    @Column(name = "destination_place_id",
        updatable = false, insertable = false)
    private String destinationPlaceId;

    /**
     * End address
     */
    @Column(name = "destination_name")
    private String destinationName;

    /**
     * End date
     */
    @Column(name = "end_date")
    private OffsetDateTime endDate;


    /**
     * Start Location
     */
    @OneToOne
    @JoinColumn(name = "origin_place_id", referencedColumnName = "location_id")
    private CommonLocation startLocation;

    /**
     * End Location
     */
    @OneToOne
    @JoinColumn(name = "destination_place_id", referencedColumnName = "location_id")
    private CommonLocation endLocation;

    public TripInfo() {
    }


    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    public OffsetDateTime getEndDate() {
        return this.endDate;
    }


    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }


    public void setOriginPlaceId(String originPlaceId) {
        this.originPlaceId = originPlaceId;
    }

    public String getDestinationPlaceId() {
        return destinationPlaceId;
    }

    public void setDestinationPlaceId(String destinationPlaceId) {
        this.destinationPlaceId = destinationPlaceId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }


    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getOriginPlaceId() {
        return originPlaceId;
    }

    public CommonLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(CommonLocation startLocation) {
        this.startLocation = startLocation;
    }

    public CommonLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(CommonLocation endLocation) {
        this.endLocation = endLocation;
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
        return "TripInfo{" +
            "tripId=" + tripId +
            ", trip=" + trip +
            ", originPlaceId='" + originPlaceId + '\'' +
            ", originName='" + originName + '\'' +
            ", startDate=" + startDate +
            ", destinationPlaceId='" + destinationPlaceId + '\'' +
            ", destinationName='" + destinationName + '\'' +
            ", endDate=" + endDate +
            ", startLocation=" + startLocation +
            ", endLocation=" + endLocation +
            '}';
    }
}
