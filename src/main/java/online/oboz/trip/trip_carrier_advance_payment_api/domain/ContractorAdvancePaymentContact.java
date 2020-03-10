package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contractor_advance_payment_contact", schema = "common")
@Data
@Accessors(chain = true)
public class ContractorAdvancePaymentContact {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Long contractorId;
    private String fullName;
    private String email;
    private String phone;
    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(OffsetDateTime.now());
        }
        onUpdate();
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(OffsetDateTime.now());
    }

}
