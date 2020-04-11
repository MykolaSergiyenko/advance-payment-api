package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "common", name = "contractor_advance_exclusion")
public class ContractorAdvanceExclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Long orderTypeId;
    private Long carrierId;
    private String carrierFullName;
    private Boolean isConfirmAdvance;
    private OffsetDateTime deletedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ContractorAdvanceExclusion() {
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

    public Long getOrderTypeId() {
        return this.orderTypeId;
    }

    public Long getCarrierId() {
        return this.carrierId;
    }

    public String getCarrierFullName() {
        return this.carrierFullName;
    }

    public Boolean getIsConfirmAdvance() {
        return this.isConfirmAdvance;
    }

    public OffsetDateTime getDeletedAt() {
        return this.deletedAt;
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

    public void setOrderTypeId(Long orderTypeId) {
        this.orderTypeId = orderTypeId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public void setCarrierFullName(String carrierFullName) {
        this.carrierFullName = carrierFullName;
    }

    public void setIsConfirmAdvance(Boolean isConfirmAdvance) {
        this.isConfirmAdvance = isConfirmAdvance;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
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

    @Override
    public String toString() {
        return "ContractorAdvanceExclusion{" +
            "id=" + id +
            ", orderTypeId=" + orderTypeId +
            ", carrierId=" + carrierId +
            ", carrierFullName='" + carrierFullName + '\'' +
            ", isConfirmAdvance=" + isConfirmAdvance +
            ", deletedAt=" + deletedAt +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
