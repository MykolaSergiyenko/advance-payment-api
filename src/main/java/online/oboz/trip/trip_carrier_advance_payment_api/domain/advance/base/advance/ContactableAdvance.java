package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.TripPaymentContractor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;


/**
 * "Аванс с уведомлениями"
 * - в каждом авансе должно быть контактное лицо для уведомлений;
 * - отправка сообщений по авансу, а так же их прочтение фиксируется в данных полях.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ContactableAdvance extends TripsAdvance {

    /**
     * isNotifiableAdvance - признак того, что можем отправить "уведомления" по авансу,
     * т.к. контакты в авансе валидны, все данные для уведомления на месте.
     * По умолчанию - false.
     */
    @Column(name = "is_notifiable", columnDefinition = "boolean default false")
    private Boolean isNotifiableAdvance;


    /**
     * Advance was notified (in some way) at
     */
    @Column(name = "notified_at")
    private OffsetDateTime notifiedAt;

    /**
     * Advance was scheduled notified (in some way) at
     */
    @Column(name = "notified_delayed_at")
    private OffsetDateTime notifiedDelayedAt;


    /**
     * Moment of e-mail was sent
     */
    @Column(name = "email_sent_at")
    private OffsetDateTime emailSentAt;


    /**
     * Moment of sms was sent
     */
    @Column(name = "sms_sent_at")
    private OffsetDateTime smsSentAt;


    /**
     * Notification (email or sms - first of them)  was read at
     * (contact go by link in letter)
     */
    @Column(name = "read_at")
    private OffsetDateTime readAt;


    /**
     * Advance-contact of Contractor
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_id", referencedColumnName = "contractor_id")
    private AdvanceContactsBook contact;


    /**
     * Contractor
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_id", insertable = false, updatable = false)
    private AdvanceContractor contractor;


    /**
     * PaymentContractor
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_contractor_id", insertable = false, updatable = false)
    private TripPaymentContractor paymentContractor;


    public ContactableAdvance() {

    }

    public void setContact(AdvanceContactsBook contact) {
        this.contact = contact;
    }


    public AdvanceContactsBook getContact() {
        return contact;
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


    public AdvanceContractor getContractor() {
        return contractor;
    }

    public void setContractor(AdvanceContractor contractor) {
        this.contractor = contractor;
    }

    public TripPaymentContractor getPaymentContractor() {
        return paymentContractor;
    }

    public void setPaymentContractor(TripPaymentContractor paymentContractor) {
        this.paymentContractor = paymentContractor;
    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
        setNotifiableAdvance(null);
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
            "isNotifiableAdvance=" + isNotifiableAdvance +
            ", notifiedAt=" + notifiedAt +
            ", notifiedDelayedAt=" + notifiedDelayedAt +
            ", emailSentAt=" + emailSentAt +
            ", smsSentAt=" + smsSentAt +
            ", readAt=" + readAt +
            ", contact=" + contact +
            ", contractor=" + contractor +
            ", paymentContractor=" + paymentContractor +
            '}';
    }
}
