package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contractor_advance_payment_contact", schema = "common")
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

    public ContractorAdvancePaymentContact() {
    }

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

    public Long getId() {
        return this.id;
    }

    public Long getContractorId() {
        return this.contractorId;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public @NotNull OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public @NotNull OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreatedAt(@NotNull OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(@NotNull OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return "ContractorAdvancePaymentContact(id=" +
            this.getId() + ", contractorId=" +
            this.getContractorId() + ", fullName=" +
            this.getFullName() + ", email=" +
            this.getEmail() + ", phone=" +
            this.getPhone() + ", createdAt=" +
            this.getCreatedAt() + ", updatedAt=" +
            this.getUpdatedAt() + ")";
    }
}
