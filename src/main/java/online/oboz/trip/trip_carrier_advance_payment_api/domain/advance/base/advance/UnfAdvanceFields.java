package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class UnfAdvanceFields extends CancelableAdvance {
    final static Logger log = LoggerFactory.getLogger(UnfAdvanceFields.class);


    @Column(name = "is_unf_send", columnDefinition = "boolean default false")
    private Boolean isUnfSend;

    @Column(name = "is_paid", columnDefinition = "boolean default false")
    private Boolean isPaid;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "is_1c_send_allowed", columnDefinition = "boolean default false")
    private Boolean is1CSendAllowed;

    @Column(name = "page_carrier_url_is_access", columnDefinition = "boolean default true")
    private Boolean pageCarrierUrlIsAccess;

    @Column(name = "is_pushed_unf_button", columnDefinition = "boolean default false")
    private Boolean isPushedUnfButton;

    @PrePersist
    private void setAutoFields() {
        if (isUnfSend() == null || isPaid() == null || is1CSendAllowed() == null || isCarrierPageAccess() == null ||
            isPushedUnfButton() == null) {
                setUnfSend(false);
                setIs1CSendAllowed(false);
                setUnfSend(false);
                setCarrierPageAccess(true);
                setPaid(false);
                setPushedUnfButton(false);
        }
    }




    @PreUpdate
    public void onUpdate() {
        //log.info("**** On-update CancelableAdvance:" + this.toString());
//
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

    public Boolean isCarrierPageAccess() {
        return pageCarrierUrlIsAccess;
    }

    public void setCarrierPageAccess(Boolean pageCarrierUrlIsAccess) {
        this.pageCarrierUrlIsAccess = pageCarrierUrlIsAccess;
    }

    public Boolean isPushedUnfButton() {
        return isPushedUnfButton;
    }

    public void setPushedUnfButton(Boolean pushedUnfButton) {
        isPushedUnfButton = pushedUnfButton;
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
            "isUnfSend=" + isUnfSend +
            ", isPaid=" + isPaid +
            ", paidAt=" + paidAt +
            ", is1CSendAllowed=" + is1CSendAllowed +
            ", pageCarrierUrlIsAccess=" + pageCarrierUrlIsAccess +
            ", isPushedUnfButton=" + isPushedUnfButton +
            '}';
    }
}
