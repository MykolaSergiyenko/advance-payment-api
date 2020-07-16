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
    final static Logger log = LoggerFactory.getLogger(UnfAdvanceFields.class);

    /**
     * Used in 'integration1c' only?
     * TODO: Is it necessary?
     */
    @Column(name = "is_unf_send", columnDefinition = "boolean default false")
    private Boolean isUnfSend;

    /**
     * Advance was paid
     * (flag come from UNF in 'integration1c'-service)
     */
    @Column(name = "is_paid", columnDefinition = "boolean default false")
    private Boolean isPaid;

    /**
     * Advance was paid_at
     */
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    /**
     * Allowed to send in UNF:
     * - null - if 'new' advance
     * - true - if 'truck's loading complete' and 'all docs loaded'
     * - false - if already sent in UNF
     */
    @Column(name = "is_1c_send_allowed", columnDefinition = "boolean default null")
    private Boolean is1CSendAllowed;

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
        setUnfFields();
    }


    private void setUnfFields() {
        if (isUnfSend() == null ||isPaid() == null|| is1CSendAllowed() == null) {
            setIs1CSendAllowed(null);
            setPaid(false);
        }
    }


    public UnfAdvanceFields() {
    }

    public Boolean isUnfSend() {
        return isUnfSend;
    }

    public void setUnfSend(Boolean unfSend) {
        isUnfSend = unfSend;
    }


    public Boolean isPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Boolean is1CSendAllowed() {
        return is1CSendAllowed;
    }

    public void setIs1CSendAllowed(Boolean is1CSendAllowed) {
        this.is1CSendAllowed = is1CSendAllowed;
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
            ", isPaid=" + isPaid +
            ", paidAt=" + paidAt +
            ", is1CSendAllowed=" + is1CSendAllowed +
            '}';
    }
}
