package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contractor_advance_payment_contact", schema = "common")
@Data
@Accessors(chain = true)
public class ContractorAdvancePaymentContact {

    @Id
    private Long id;
    private Long contractorId;
    private String fullName;
    private String email;
    private String phone;
    private OffsetDateTime createdAt = OffsetDateTime.now();

 }
