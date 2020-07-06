package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseUpdateEntity extends BaseEntity {

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;


    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


    public BaseUpdateEntity() {
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
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

    @PrePersist
    public void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(OffsetDateTime.now());
        }
        setUpdatedAt(OffsetDateTime.now());
    }

    @PreUpdate
    public void onUpdate() {
        setUpdatedAt(OffsetDateTime.now());
    }

    @Override
    public String toString() {
        return "Base Update Entity{" +
            "createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
