package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_request_advance_payment", schema = "orders")
@Data
@Accessors(chain = true)
public class TripRequestAdvancePayment {
    //TODO: need rename table to trip_advance
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Long tripId;
    private String tripTypeCode;
    private Long driverId;
    private Long contractorId;
    private Long paymentContractorId;
    private Double tripCost;
    private Double advancePaymentSum;
    private Double registrationFee;
    private Boolean loadingComplete;
    private String uuidContractApplicationFile;
    private String uuidAdvanceApplicationFile;
    @Column(name = "is_1c_send_allowed")
    private Boolean is1CSendAllowed;
    private Boolean isCancelled;
    private String comment;
    private Boolean isPushedUnfButton;
    private Boolean isUnfSend;
    private Boolean isPaid;
    private OffsetDateTime paidAt;
    private Boolean pageCarrierUrlIsAccess;
    private Boolean isAutomationRequest;
    private Boolean isDownloadedContractApplication;
    private Boolean isDownloadedAdvanceApplication;
    private Long authorId;
    private String cancelledComment;
    private OffsetDateTime pushButtonAt;
    private UUID advanceUuid;

    private OffsetDateTime emailReadAt;
    private Boolean isEmailRead;
    private Boolean isSmsSent;


    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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
}
