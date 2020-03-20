package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(schema = "orders", name = "orders")
public class Order {
    @Id
    private Long id;
    private String orderNum;
    private String orderStatus;
    private String originOrderNum;
    private Long orderTypeId;
    private String contractPaymentContractorName;
}
