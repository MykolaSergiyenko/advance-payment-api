package online.oboz.trip.trip_carrier_advance_payment_api.util;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;

public class DtoUtils {
    public static MessageDto getMessageDto(
        TripRequestAdvancePayment tripRequestAdvancePayment,
        ContractorAdvancePaymentContact contact,
        String paymentContractorFullName,
        String lkUrl,
        String tripNum
    ) {
        MessageDto messageDto = new MessageDto();
        messageDto.setAdvancePaymentSum(tripRequestAdvancePayment.getAdvancePaymentSum());
        messageDto.setContractorName(paymentContractorFullName);
        messageDto.setEmail(contact.getEmail());
        messageDto.setPhone(contact.getPhone());
        messageDto.setLKLink(lkUrl + tripRequestAdvancePayment.getAdvanceUuid());
        messageDto.setTripNum(tripNum);
        return messageDto;
    }
}
