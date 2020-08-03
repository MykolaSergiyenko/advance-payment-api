package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * "Аванс для УНФ"
 * - здесь описаны поля связанные с интеграцией Аванса и УНФ.
 * - большинство полей дублировало логику друг друга.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class UnfAdvanceFields extends CancelableAdvance {

    /**
     * Advance was cancelled at
     */
    @Column(name = "unf_sent_at")
    private OffsetDateTime unfSentAt;


    /**
     * Advance was paid_at
     */
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;


    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
    }


    public UnfAdvanceFields() {
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public OffsetDateTime getUnfSentAt() {
        return unfSentAt;
    }

    public void setUnfSentAt(OffsetDateTime unfSentAt) {
        this.unfSentAt = unfSentAt;
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
        return "UnfAdvanceFields{" +
            "unfSentAt=" + unfSentAt +
            ", paidAt=" + paidAt +
            '}';
    }
}
