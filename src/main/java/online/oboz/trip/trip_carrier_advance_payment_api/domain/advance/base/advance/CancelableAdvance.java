package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts.HasContractor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;


/**
 * "Отменяемый аванс"
 * - поля аванса, связанные с отменой, созданием, комментированием
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@AttributeOverride(name = "contractorId",
    column = @Column(name = "contractor_id", insertable = false, updatable = false))
public abstract class CancelableAdvance extends HasContractor {


    /**
     * Comment on Advance:
     * - auto-advances have 'auto created' comments
     * - problem advances have other comments
     * - other advances haven't comments
     */
    @Column(name = "comment")
    private String comment;


    /**
     * Cancellation comment on Advance
     */
    @Column(name = "cancelled_comment")
    private String cancelledComment;


    /**
     * Advance was cancelled
     */
    @Column(name = "is_cancelled", columnDefinition = "boolean default false", nullable = false)
    private Boolean isCancelled;


    /**
     * Advance was cancelled at
     */
    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;


    /**
     * Auto-created advance (default false)
     */
    @Column(name = "is_automation_request", columnDefinition = "boolean default false", nullable = false)
    private Boolean isAuto = false;


    /**
     * @param autoComment
     * @return Has advance non-auto comment
     */
    public boolean isProblem(String autoComment) {
        return !(this.comment == null || this.comment.isEmpty() || this.comment.equals(autoComment));
    }


    public CancelableAdvance() {

    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
        setCancelled(false);
    }


    @Override
    @PreUpdate
    public void onUpdate() {
        super.onUpdate();
    }


    public Boolean isAuto() {
        return isAuto;
    }

    public void setIsAuto(Boolean auto) {
        isAuto = auto;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCancelledComment() {
        return cancelledComment;
    }

    public void setCancelledComment(String cancelledComment) {
        this.cancelledComment = cancelledComment;
    }

    public Boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
        return "CancelableAdvance{" +
            super.toString() +
            ", comment='" + comment + '\'' +
            ", cancelledComment='" + cancelledComment + '\'' +
            ", isCancelled=" + isCancelled +
            ", cancelledAt=" + cancelledAt +
            ", isAuto=" + isAuto +
            '}';
    }
}
