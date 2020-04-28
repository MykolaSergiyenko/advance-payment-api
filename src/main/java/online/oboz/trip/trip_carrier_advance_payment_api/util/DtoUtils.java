package online.oboz.trip.trip_carrier_advance_payment_api.util;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;

public class DtoUtils {
    private final ContractorRepository contractorRepository;
    private final ApplicationProperties applicationProperties;


    public DtoUtils(ContractorRepository contractorRepository, ApplicationProperties applicationProperties) {
        this.contractorRepository = contractorRepository;
        this.applicationProperties = applicationProperties;
    }


    public MessageDto newMessage(
        TripRequestAdvancePayment tripRequestAdvancePayment,
        ContractorAdvancePaymentContact contact,
        String tripNum) {

        String lkUrl = applicationProperties.getLkUrl();

        String paymentContractor =
            contractorRepository.getPaymentContractorName(tripRequestAdvancePayment.getPaymentContractorId());

        MessageDto messageDto = new MessageDto();
        messageDto.setAdvancePaymentSum(tripRequestAdvancePayment.getAdvancePaymentSum());
        messageDto.setContractorName(paymentContractor);
        messageDto.setEmail(contact.getEmail());
        messageDto.setPhone(contact.getPhone());
        messageDto.setLKLink(lkUrl + tripRequestAdvancePayment.getAdvanceUuid());
        messageDto.setTripNum(tripNum);
        return messageDto;
    }
}
