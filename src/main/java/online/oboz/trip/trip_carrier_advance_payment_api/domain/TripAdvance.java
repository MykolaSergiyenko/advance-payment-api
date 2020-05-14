package online.oboz.trip.trip_carrier_advance_payment_api.domain;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_request_advance_payment", schema = "orders")
public class TripAdvance {
    //TODO: need rename table to trip_advance
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Column(name = "trip_id", updatable = false, insertable = false)
    private Long tripId;

    @NotNull
    @Column(name = "trip_type_code", updatable = false, insertable = false)
    private String tripTypeCode;


    @OneToOne(mappedBy = "trip_advance", fetch = FetchType.LAZY)
    private Trip trip;

    @NotNull
    @Column(name = "driver_id", updatable = false, insertable = false)
    private Long driverId;
    @NotNull
    @Column(name = "contractor_id", updatable = false, insertable = false)
    private Long contractorId;
    @NotNull
    @Column(name = "payment_contractor_id", updatable = false, insertable = false)
    private Long paymentContractorId;

    @NotNull
    @Column(name="trip_cost", columnDefinition="Decimal(19,2) default '0.00'")
    private Double tripCost;

    @NotNull
    @Column(name="advance_payment_sum", columnDefinition="Decimal(19,2) default '0.00'")
    private Double advancePaymentSum;

    @NotNull
    @Column(name="registration_fee", columnDefinition="Numeric(19,2) default '0.00'")
    private Double registrationFee;

    @Column(name="comment", columnDefinition="varchar(255) default ''")
    private String comment;

    @Column(columnDefinition="varchar(255) default ''")
    private Long authorId;

    private String cancelledComment;
    private OffsetDateTime pushButtonAt;
    private OffsetDateTime emailReadAt;
    private OffsetDateTime paidAt;


    @NotNull
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID advanceUuid;


    //TODO: @Type(type = "uuid-char")@Column(length = 36)
    @Column(name = "uuid_contract_application_file")
    private String uuidContractApplicationFile;

    //TODO: @Type(type = "uuid-char")@Column(length = 36)
    @Column(name = "uuid_advance_application_file")
    private String uuidAdvanceApplicationFile;


    @Column(name = "is_automation_request", columnDefinition = "boolean default false", nullable = false)
    private Boolean isAuto;

    @Column(name = "is_1c_send_allowed", columnDefinition = "boolean default true", nullable = false)
    private Boolean is1CSendAllowed;
    @Column(columnDefinition = "boolean default true", nullable = false)
    private Boolean pageCarrierUrlIsAccess;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean loadingComplete;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isDownloadedContractApplication;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isDownloadedAdvanceApplication;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isPushedUnfButton;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isUnfSend;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isPaid;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isCancelled;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isSmsSent;
    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isEmailRead;


    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public TripAdvance() {

    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
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

    public Long getTripId() {
        return this.tripId;
    }

    public TripAdvance setTripId(Long tripId) {
        this.tripId = tripId;
        return this;
    }

    public String getTripTypeCode() {
        return this.tripTypeCode;
    }

    public Long getDriverId() {
        return this.driverId;
    }

    public Long getContractorId() {
        return this.contractorId;
    }

    public Long getPaymentContractorId() {
        return this.paymentContractorId;
    }

    public Double getTripCost() {
        return this.tripCost;
    }

    public Double getAdvancePaymentSum() {
        return this.advancePaymentSum;
    }

    public Double getRegistrationFee() {
        return this.registrationFee;
    }

    public Boolean getLoadingComplete() {
        return this.loadingComplete;
    }

    public String getUuidContractApplicationFile() {
        return this.uuidContractApplicationFile;
    }

    public String getUuidAdvanceApplicationFile() {
        return this.uuidAdvanceApplicationFile;
    }

    public Boolean getIs1CSendAllowed() {
        return this.is1CSendAllowed;
    }

    public Boolean getIsCancelled() {
        return this.isCancelled;
    }

    public String getComment() {
        return this.comment;
    }

    public Boolean getIsPushedUnfButton() {
        return this.isPushedUnfButton;
    }

    public Boolean getIsUnfSend() {
        return this.isUnfSend;
    }


    public TripAdvance setUnfSend(Boolean unfSend) {
        isUnfSend = unfSend;
        return this;
    }


    public Boolean getIsPaid() {
        return this.isPaid;
    }

    public OffsetDateTime getPaidAt() {
        return this.paidAt;
    }

    public Boolean getPageCarrierUrlIsAccess() {
        return this.pageCarrierUrlIsAccess;
    }

    public Boolean getIsAuto() {
        return this.isAuto;
    }

    public Boolean getIsDownloadedContractApplication() {
        return this.isDownloadedContractApplication;
    }

    public Boolean getIsDownloadedAdvanceApplication() {
        return this.isDownloadedAdvanceApplication;
    }

    public Long getAuthorId() {
        return this.authorId;
    }

    public String getCancelledComment() {
        return this.cancelledComment;
    }

    public OffsetDateTime getPushButtonAt() {
        return this.pushButtonAt;
    }

    public UUID getAdvanceUuid() {
        return this.advanceUuid;
    }

    public OffsetDateTime getEmailReadAt() {
        return this.emailReadAt;
    }

    public Boolean getIsEmailRead() {
        return this.isEmailRead;
    }

    public Boolean getIsSmsSent() {
        return this.isSmsSent;
    }

    public @NotNull OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public @NotNull OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public TripAdvance setId(Long id) {
        this.id = id;
        return this;
    }



    public TripAdvance setTripTypeCode(String tripTypeCode) {
        this.tripTypeCode = tripTypeCode;
        return this;
    }

    public TripAdvance setDriverId(Long driverId) {
        this.driverId = driverId;
        return this;
    }

    public TripAdvance setContractorId(Long contractorId) {
        this.contractorId = contractorId;
        return this;
    }

    public TripAdvance setPaymentContractorId(Long paymentContractorId) {
        this.paymentContractorId = paymentContractorId;
        return this;
    }

    public TripAdvance setTripCost(Double tripCost) {
        this.tripCost = tripCost;
        return this;
    }

    public TripAdvance setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
        return this;
    }

    public TripAdvance setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
        return this;
    }

    public TripAdvance setLoadingComplete(Boolean loadingComplete) {
        this.loadingComplete = loadingComplete;
        return this;
    }

    public TripAdvance setUuidContractApplicationFile(String uuidContractApplicationFile) {
        this.uuidContractApplicationFile = uuidContractApplicationFile;
        return this;
    }

    public TripAdvance setUuidAdvanceApplicationFile(String uuidAdvanceApplicationFile) {
        this.uuidAdvanceApplicationFile = uuidAdvanceApplicationFile;
        return this;
    }

    public TripAdvance setIs1CSendAllowed(Boolean is1CSendAllowed) {
        this.is1CSendAllowed = is1CSendAllowed;
        return this;
    }

    public TripAdvance setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
        return this;
    }

    public TripAdvance setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public TripAdvance setIsPushedUnfButton(Boolean isPushedUnfButton) {
        this.isPushedUnfButton = isPushedUnfButton;
        return this;
    }





    public TripAdvance setPageCarrierUrlIsAccess(Boolean pageCarrierUrlIsAccess) {
        this.pageCarrierUrlIsAccess = pageCarrierUrlIsAccess;
        return this;
    }

    public TripAdvance setIsAuto(Boolean isAutoRequest) {
        this.isAuto = isAutoRequest;
        return this;
    }


    public TripAdvance setIsDownloadedContractApplication(Boolean isDownloadedContractApplication) {
        this.isDownloadedContractApplication = isDownloadedContractApplication;
        return this;
    }

    public TripAdvance setIsDownloadedAdvanceApplication(Boolean isDownloadedAdvanceApplication) {
        this.isDownloadedAdvanceApplication = isDownloadedAdvanceApplication;
        return this;
    }

    public TripAdvance setAuthorId(Long authorId) {
        this.authorId = authorId;
        return this;
    }

    public TripAdvance setCancelledComment(String cancelledComment) {
        this.cancelledComment = cancelledComment;
        return this;
    }

    public TripAdvance setPushButtonAt(OffsetDateTime pushButtonAt) {
        this.pushButtonAt = pushButtonAt;
        return this;
    }

    public TripAdvance setAdvanceUuid(UUID advanceUuid) {
        this.advanceUuid = advanceUuid;
        return this;
    }

    public TripAdvance setEmailReadAt(OffsetDateTime emailReadAt) {
        this.emailReadAt = emailReadAt;
        return this;
    }

    public TripAdvance setIsEmailRead(Boolean isEmailRead) {
        this.isEmailRead = isEmailRead;
        return this;
    }

    public TripAdvance setIsSmsSent(Boolean isSmsSent) {
        this.isSmsSent = isSmsSent;
        return this;
    }

    public TripAdvance setCreatedAt(@NotNull OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TripAdvance setUpdatedAt(@NotNull OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
