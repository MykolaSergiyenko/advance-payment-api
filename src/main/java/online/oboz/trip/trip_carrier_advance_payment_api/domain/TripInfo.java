package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Data
@Table(schema = "orders", name = "trip_infos")
public class TripInfo {
    @Id
    private Long id;
    private Long tripId;
    public OffsetDateTime startDate;
    public OffsetDateTime endDate;
}
