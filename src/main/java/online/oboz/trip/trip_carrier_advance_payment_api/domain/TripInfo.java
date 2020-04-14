package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "orders", name = "trip_infos")
public class TripInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "trip_id", updatable = false, insertable = false)
    private Long tripId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private String originPlaceId;
    private String originName;
    private OffsetDateTime startDate;

    private String destinationPlaceId;
    private String destinationName;
    private OffsetDateTime endDate;

    public TripInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getTripId() {
        return this.tripId;
    }

    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    public OffsetDateTime getEndDate() {
        return this.endDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }


    public String getOriginPlaceId() {
        return originPlaceId;
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

    @Override
    public String toString() {
        return "TripInfo{" +
            "id=" + id +
            ", tripId=" + tripId +
            ", originPlaceId='" + originPlaceId + '\'' +
            ", originName='" + originName + '\'' +
            ", startDate=" + startDate +
            ", destinationPlaceId='" + destinationPlaceId + '\'' +
            ", destinationName='" + destinationName + '\'' +
            ", endDate=" + endDate +
            '}';
    }
}
