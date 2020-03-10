package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
@Data
@Accessors(chain = true)
public class AdvancePaymentCost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Double minValue;
    private Double maxValue;
    private Double advancePaymentSum;
    private Double registrationFee;
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
