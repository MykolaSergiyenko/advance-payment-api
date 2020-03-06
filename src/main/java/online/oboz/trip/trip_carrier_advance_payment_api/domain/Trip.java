package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(schema = "orders", name = "trips")
public class Trip {
    @Id
    private Long id;
    private Long contractorId;
    private Long driverId;
    private String num;
    private Long orderId;
    private Double cost;
    private Long paymentContractorId;
    private String vatCode;
    private String tripTypeCode;
    private String resourceTypeCode;
    private Boolean isAdvancedPayment;

}
