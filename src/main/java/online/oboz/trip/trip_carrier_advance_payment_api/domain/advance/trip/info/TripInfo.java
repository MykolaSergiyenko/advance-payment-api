package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "orders", name = "trip_infos")
public class TripInfo extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(name = "trip_id", updatable = false, insertable = false)
    private Long tripId;

//    @OneToOne
//    @JoinColumn(name = "trip_id")
//    private Trip trip;


//    @OneToMany
//    @JoinColumns({
//        @JoinColumn(name = "origin_place_id"),
//        @JoinColumn(name = "destination_place_id")
//    })
//    private Set<CommonLocation> locations;

    @Column(name = "origin_place_id", updatable = false, insertable = false)
    private String originPlaceId;


    @Column(name = "origin_name")
    private String originName;

    @Column(name = "start_date")
    private OffsetDateTime startDate;


    @Column(name = "destination_place_id",
        updatable = false, insertable = false)
    private String destinationPlaceId;

    @Column(name = "destination_name")
    private String destinationName;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    public TripInfo() {
    }



//    public Set<CommonLocation> getLocations() {
//        return locations;
//    }
//
//    public void setLocations(Set<CommonLocation> locations) {
//        this.locations = locations;
//    }



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


//    public void setOriginLocation(CommonLocation originLocation) {
//        this.locations.add(originLocation);
//    }


//    public CommonLocation getDestLocation() {
//        return destLocation;
//    }

//    public void setDestLocation(CommonLocation destLocation) {
//        this.locations.add(destLocation);
//    }

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

//            ", trip=" + trip +
//            ", locations=" + locations +
            ", originPlaceId='" + originPlaceId + '\'' +
            ", originName='" + originName + '\'' +
            ", startDate=" + startDate +
            ", destinationPlaceId='" + destinationPlaceId + '\'' +
            ", destinationName='" + destinationName + '\'' +
            ", endDate=" + endDate +
            '}';
    }
}
