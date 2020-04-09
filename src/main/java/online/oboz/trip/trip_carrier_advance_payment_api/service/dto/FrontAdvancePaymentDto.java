package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class FrontAdvancePaymentDto {
    private Long tripId;
    private String tripTypeCode;
    private String num;
    private Long driverId;
    private Long contractorId;
    private Boolean isAutomationRequest;
    private String paymentContractor;
    private Double tripCostWithVat;
    private Double advancePaymentSum;
    private Double registrationFee;
    private Boolean loadingComplete;
    private Boolean cancelAdvance;
    private String comment;
    private String cancelAdvanceComment;
    private Boolean isUnfSend;
    private Boolean isPaid;
    private OffsetDateTime paidAt;
    private Boolean pageCarrierUrlIsAccess;
    private Long authorId;
    private String contact;
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private OffsetDateTime updatedAt;

    private OffsetDateTime loadingDate;
    private OffsetDateTime unloadingDate;

    public FrontAdvancePaymentDto() {
    }

    public Long getTripId() {
        return this.tripId;
    }

    public String getTripTypeCode() {
        return this.tripTypeCode;
    }

    public String getNum() {
        return this.num;
    }

    public Long getDriverId() {
        return this.driverId;
    }

    public Long getContractorId() {
        return this.contractorId;
    }

    public Boolean getIsAutomationRequest() {
        return this.isAutomationRequest;
    }

    public String getPaymentContractor() {
        return this.paymentContractor;
    }

    public Double getTripCostWithVat() {
        return this.tripCostWithVat;
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

    public Boolean getCancelAdvance() {
        return this.cancelAdvance;
    }

    public String getComment() {
        return this.comment;
    }

    public String getCancelAdvanceComment() {
        return this.cancelAdvanceComment;
    }

    public Boolean getIsUnfSend() {
        return this.isUnfSend;
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

    public Long getAuthorId() {
        return this.authorId;
    }

    public String getContact() {
        return this.contact;
    }

    public @NotNull OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public @NotNull OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public OffsetDateTime getLoadingDate() {
        return this.loadingDate;
    }

    public OffsetDateTime getUnloadingDate() {
        return this.unloadingDate;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public void setTripTypeCode(String tripTypeCode) {
        this.tripTypeCode = tripTypeCode;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public void setIsAutomationRequest(Boolean isAutomationRequest) {
        this.isAutomationRequest = isAutomationRequest;
    }

    public void setPaymentContractor(String paymentContractor) {
        this.paymentContractor = paymentContractor;
    }

    public void setTripCostWithVat(Double tripCostWithVat) {
        this.tripCostWithVat = tripCostWithVat;
    }

    public void setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public void setLoadingComplete(Boolean loadingComplete) {
        this.loadingComplete = loadingComplete;
    }

    public void setCancelAdvance(Boolean cancelAdvance) {
        this.cancelAdvance = cancelAdvance;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCancelAdvanceComment(String cancelAdvanceComment) {
        this.cancelAdvanceComment = cancelAdvanceComment;
    }

    public void setIsUnfSend(Boolean isUnfSend) {
        this.isUnfSend = isUnfSend;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setPageCarrierUrlIsAccess(Boolean pageCarrierUrlIsAccess) {
        this.pageCarrierUrlIsAccess = pageCarrierUrlIsAccess;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setCreatedAt(@NotNull OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(@NotNull OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLoadingDate(OffsetDateTime loadingDate) {
        this.loadingDate = loadingDate;
    }

    public void setUnloadingDate(OffsetDateTime unloadingDate) {
        this.unloadingDate = unloadingDate;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return "FrontAdvancePaymentDto(tripId=" +
            this.getTripId() + ", tripTypeCode=" +
            this.getTripTypeCode() + ", num=" +
            this.getNum() + ", driverId=" +
            this.getDriverId() + ", contractorId=" +
            this.getContractorId() + ", isAutomationRequest=" +
            this.getIsAutomationRequest() + ", paymentContractor=" +
            this.getPaymentContractor() + ", tripCostWithVat=" +
            this.getTripCostWithVat() + ", advancePaymentSum=" +
            this.getAdvancePaymentSum() + ", registrationFee=" +
            this.getRegistrationFee() + ", loadingComplete=" +
            this.getLoadingComplete() + ", cancelAdvance=" +
            this.getCancelAdvance() + ", comment=" +
            this.getComment() + ", cancelAdvanceComment=" +
            this.getCancelAdvanceComment() + ", isUnfSend=" +
            this.getIsUnfSend() + ", isPaid=" +
            this.getIsPaid() + ", paidAt=" +
            this.getPaidAt() + ", pageCarrierUrlIsAccess=" +
            this.getPageCarrierUrlIsAccess() + ", authorId=" +
            this.getAuthorId() + ", contact=" +
            this.getContact() + ", createdAt=" +
            this.getCreatedAt() + ", updatedAt=" +
            this.getUpdatedAt() + ", loadingDate=" +
            this.getLoadingDate() + ", unloadingDate=" +
            this.getUnloadingDate() + ")";
    }
}
