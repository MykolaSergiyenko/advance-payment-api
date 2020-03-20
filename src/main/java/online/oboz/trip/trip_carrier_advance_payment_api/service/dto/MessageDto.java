package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Accessors(chain = true)
public class MessageDto {
    private String tripNum;
    private String contractorName;
    private String email;
    private Double advancePaymentSum;
    private String phone;
    private String lKLink;
}
