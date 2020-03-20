package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
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
}
