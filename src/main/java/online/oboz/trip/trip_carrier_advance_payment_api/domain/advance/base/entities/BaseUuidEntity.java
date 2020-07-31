package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Базовая сущность с UUID
 *
 * @author Ⓐboz
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseUuidEntity extends BaseUpdateEntity {
    final static Logger log = LoggerFactory.getLogger(BaseUuidEntity.class);

    /**
     * UUID сущностей
     */
    @NaturalId
    @NotNull
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(length = 36)
    private UUID uuid;


    public BaseUuidEntity() {
    }


    public UUID getUuid() {
        return this.uuid;
    }


    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
    @Override
    public void onCreate() {
        super.onCreate();
        setUuid(UUID.randomUUID());
    }


    @Override
    public String toString() {
        return "BaseUuidEntity { uuid=" + uuid + "}\n";
    }
}
