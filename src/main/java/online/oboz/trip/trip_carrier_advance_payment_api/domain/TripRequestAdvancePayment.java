package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trip_request_advance_payment", schema = "orders")
@Data
@Accessors(chain = true)
public class TripRequestAdvancePayment {

    @Id
    private Long id;
    private Long tripId;
    private Long tripType;
    private Long driverId;
    private Long contractorId;
    private Long paymentContractorId;
    private BigDecimal tripCost;
    private Integer vat;
    private BigDecimal advancePaymentSum;
    private BigDecimal registrationFee;
    private Boolean cancelAdvance;
    private String comment;
    private Boolean isUnfSend;
    private Boolean pageCarrierUrlIsAccess;
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
