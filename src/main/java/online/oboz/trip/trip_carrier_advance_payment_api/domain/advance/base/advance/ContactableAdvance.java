package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;


// All about Create "Advance" and its "notifications"

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ContactableAdvance extends TripsAdvance {
    private static final Logger log = LoggerFactory.getLogger(ContactableAdvance.class);

    /**
     * isNotifiableAdvance - признак того, что можем отправить "уведомления" по авансу,
     * т.к. контакты в авансе валидны, все данные для уведомления на месте.
     *  По умолчанию - false.
     *
     */
    @Column(name = "is_notifiable", columnDefinition = "boolean default false")
    private Boolean isNotifiableAdvance = false;


    @Column(name = "notified_at")
    private OffsetDateTime notifiedAt;

    @Column(name = "notified_delayed_at")
    private OffsetDateTime notifiedDelayedAt;



    @Column(name = "email_sent_at")
    private OffsetDateTime emailSentAt;


    @Column(name = "sms_sent_at")
    private OffsetDateTime smsSentAt;



    // set email-read at, when driver go to lk-link
    @Column(name = "read_at")
    private OffsetDateTime readAt;


    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_id", referencedColumnName = "contractor_id")
    private AdvanceContactsBook contact;



    public ContactableAdvance() {

    }


    public Boolean isNotifiableAdvance() {
        return isNotifiableAdvance;
    }




    public OffsetDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(OffsetDateTime emailToContactSentAt) {
        this.emailSentAt = emailToContactSentAt;
    }


    public OffsetDateTime getSmsSentAt() {
        return smsSentAt;
    }

    public void setSmsSentAt(OffsetDateTime smsToContactSentAt) {
        this.smsSentAt = smsToContactSentAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }


    public OffsetDateTime getReadAt() {
        return readAt;
    }



    public AdvanceContactsBook getContact() {
        return contact;
    }


    public Boolean getNotifiableAdvance() {
        return isNotifiableAdvance;
    }

    public void setNotifiableAdvance(Boolean notifiableAdvance) {
        isNotifiableAdvance = notifiableAdvance;
    }

    public OffsetDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(OffsetDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public OffsetDateTime getNotifiedDelayedAt() {
        return notifiedDelayedAt;
    }

    public void setNotifiedDelayedAt(OffsetDateTime notifiedDelayedAt) {
        this.notifiedDelayedAt = notifiedDelayedAt;
    }

    public void setContact(AdvanceContactsBook contact) {
        this.contact = contact;
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
        return "ContactableAdvance{" +
            super.toString() +
            "isNotifiableAdvance=" + isNotifiableAdvance +
            ", notifiedAt=" + notifiedAt +
            ", notifiedDelayedAt=" + notifiedDelayedAt +
            ", emailSentAt=" + emailSentAt +
            ", smsSentAt=" + smsSentAt +
            ", readAt=" + readAt +
            ", contact=" + contact +
            '}';
    }
}
