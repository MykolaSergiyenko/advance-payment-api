package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(schema = "common", name = "contractors")
public class Contractor {

    @Id
    private Long id;
    private String fullName;
    private Boolean isVatPayer;
    private Boolean isVerified;
    private String phone;
    private String email;
    private Boolean isAutoAdvancePayment;

}
