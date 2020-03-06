package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
@Data
@Accessors(chain = true)
public class AdvancePaymentCost {

    @Id
    private Long id;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal advancePaymentSum;
    private BigDecimal registrationFee;
    private OffsetDateTime createdAt = OffsetDateTime.now();

 }
