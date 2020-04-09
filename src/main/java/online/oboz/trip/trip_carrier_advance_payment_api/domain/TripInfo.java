package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "orders", name = "trip_infos")
public class TripInfo {
    @Id
    private Long id;
    private Long tripId;
    public OffsetDateTime startDate;
    public OffsetDateTime endDate;

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

    public String toString() {
        return "TripInfo(id=" +
            this.getId() + ", tripId=" +
            this.getTripId() + ", startDate=" +
            this.getStartDate() + ", endDate=" +
            this.getEndDate() + ")";
    }
}
