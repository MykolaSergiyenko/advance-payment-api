package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Contractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvancePaymentContact;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRequestRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AutoAdvancedService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvancePageDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Paginator;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseAdvancePayment;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdvancePageTabsService {

    private final AdvanceRequestRepository advanceRequestRepository;
    private final ContractorRepository contractorRepository;
    private final AdvanceContactRepository advanceContactRepository;
    private final TripRepository tripRepository;

    public AdvancePageTabsService(
        AdvanceRequestRepository advanceRequestRepository,
        AdvanceContactRepository advanceContactRepository,
        ContractorRepository contractorRepository,
        TripRepository tripRepository
    ) {
        this.advanceRequestRepository = advanceRequestRepository;
        this.contractorRepository = contractorRepository;
        this.advanceContactRepository = advanceContactRepository;
        this.tripRepository = tripRepository;
    }

    public ResponseEntity<ResponseAdvancePayment> searchInWorkRequests(Filter filter) {
        List<TripRequestAdvancePayment> tripRequestAdvancePayments = advanceRequestRepository.findAll().stream()
            .filter(request -> !isProblem(request))
            .filter(request -> !isPaid(request))
            .filter(request -> !isNotPaid(request))
            .filter(request -> !isCanceled(request))
            .collect(Collectors.toList());
        return new ResponseEntity<>(mapResponse(tripRequestAdvancePayments, getFullMapper(), filter), HttpStatus.OK);
    }

    public ResponseEntity<ResponseAdvancePayment> searchProblemRequests(Filter filter) {
        List<TripRequestAdvancePayment> requests = advanceRequestRepository.findAll().stream()
            .filter(this::isProblem)
            .collect(Collectors.toList());
        return new ResponseEntity<>(mapResponse(requests, getMapperWithoutContractorInfo(), filter), HttpStatus.OK);
    }

    public ResponseEntity<ResponseAdvancePayment> searchPaidRequests(Filter filter) {
        List<TripRequestAdvancePayment> requests = advanceRequestRepository.findAll().stream()
            .filter(this::isPaid)
            .collect(Collectors.toList());
        return new ResponseEntity<>(mapResponse(requests, getMapperWithoutContractorInfo(), filter), HttpStatus.OK);
    }

    public ResponseEntity<ResponseAdvancePayment> searchNotPaidRequests(Filter filter) {
        List<TripRequestAdvancePayment> requests = advanceRequestRepository.findAll().stream()
            .filter(request -> !isCanceled(request))
            .filter(this::isNotPaid)
            .collect(Collectors.toList());
        return new ResponseEntity<>(mapResponse(requests, getMapperWithoutContractorInfo(), filter), HttpStatus.OK);
    }

    public ResponseEntity<ResponseAdvancePayment> searchCanceledRequests(Filter filter) {
        List<TripRequestAdvancePayment> requests = advanceRequestRepository.findAll().stream()
            .filter(this::isCanceled)
            .collect(Collectors.toList());
        return new ResponseEntity<>(mapResponse(requests, getMapperWithoutContractorInfo(), filter), HttpStatus.OK);
    }

    private ResponseAdvancePayment mapResponse(
        List<TripRequestAdvancePayment> tripRequestAdvancePayments,
        Function<TripRequestAdvancePayment, AdvancePageDTO> mapper,
        Filter filter
    ) {
        tripRequestAdvancePayments.sort(Comparator.comparing(TripRequestAdvancePayment::getId).reversed());
        List<AdvancePageDTO> responseList = tripRequestAdvancePayments.stream()
            .skip(getOffset(filter.getPage(), filter.getPer()))
            .limit(filter.getPer())
            .map(mapper)
            .collect(Collectors.toList());

        ResponseAdvancePayment responseAdvancePayment = new ResponseAdvancePayment();
        responseAdvancePayment.setRequestAdvancePayment(responseList);
        responseAdvancePayment.setPaginator(
            new Paginator()
                .page(filter.getPage())
                .per(filter.getPer())
                .total(tripRequestAdvancePayments.size())
        );

        return responseAdvancePayment;
    }

    private boolean isCanceled(TripRequestAdvancePayment request) {
        return request.getIsCancelled();
    }

    private boolean isNotPaid(TripRequestAdvancePayment request) {
        return request.getIsDownloadedAdvanceApplication() &&
            request.getIsDownloadedContractApplication() &&
            request.getIsPushedUnfButton() &&
            request.getIsUnfSend() &&
            !request.getIsPaid();
    }

    private boolean isPaid(TripRequestAdvancePayment request) {
        return request.getIsDownloadedAdvanceApplication() &&
            request.getIsDownloadedContractApplication() &&
            request.getIsPushedUnfButton() &&
            request.getIsUnfSend() &&
            request.getIsPaid() &&
            request.getPaidAt() != null;
    }

    private boolean isProblem(TripRequestAdvancePayment request) {
        return StringUtils.isNotBlank(request.getComment()) &&
            !request.getComment().equals(AutoAdvancedService.AUTO_ADVANCE_COMMENT);
    }

    //TODO переделать на маленькие dto для каждой вкладки
    private AdvancePageDTO mapAdvancePageDTO(
        TripRequestAdvancePayment rec,
        ContractorAdvancePaymentContact advanceContact,
        Contractor contractor,
        String contractorPaymentName,
        Trip trip
    ) {
        AdvancePageDTO frontAdvancePaymentResponse = new AdvancePageDTO();
        frontAdvancePaymentResponse
            .id(rec.getId())
            .tripNum(trip == null ? null : trip.getNum())
            .createdAt(trip == null ? null : trip.getCreatedAt())
            .contractorName(contractor == null ? null : contractor.getFullName())
            .contactFio(advanceContact == null ? null : advanceContact.getFullName())
            .contactPhone(advanceContact == null ? null : advanceContact.getPhone())
            .contactEmail(advanceContact == null ? null : advanceContact.getEmail())
            .paymentContractor(contractorPaymentName)
            .isAutomationRequest(rec.getIsAutomationRequest())
            .tripCostWithVat(rec.getTripCost())
            .advancePaymentSum(rec.getAdvancePaymentSum())
            .registrationFee(rec.getRegistrationFee())
            //проставляется вручную сотрудниками авансирования
            .loadingComplete(rec.getLoadingComplete())
            .urlContractApplication(rec.getUuidContractApplicationFile())
            .urlAdvanceApplication(rec.getUuidAdvanceApplicationFile())
            .is1CSendAllowed(rec.getIs1CSendAllowed())
            .isPushedUnfButton(rec.getIsPushedUnfButton())
            .isUnfSend(rec.getIsUnfSend())
            .pushButtonAt(rec.getPushButtonAt())
            .isPaid(rec.getIsPaid())
            .paidAt(rec.getPaidAt())
            .comment(rec.getComment())
            .isCancelled(rec.getIsCancelled())
            .cancelledComment(rec.getCancelledComment());
        return frontAdvancePaymentResponse;
    }

    private Function<TripRequestAdvancePayment, AdvancePageDTO> getFullMapper() {
        return request -> {
            long contractorId = request.getContractorId();
            ContractorAdvancePaymentContact advanceContact = advanceContactRepository.find(contractorId).orElse(null);
            Contractor contractor = contractorRepository.findById(contractorId).orElse(null);
            String fullName = contractorRepository.getPaymentContractorName(request.getPaymentContractorId());
            Trip trip = tripRepository.findById(request.getTripId()).orElse(null);
            return mapAdvancePageDTO(request, advanceContact, contractor, fullName, trip);
        };
    }

    private Function<TripRequestAdvancePayment, AdvancePageDTO> getMapperWithoutContractorInfo() {
        return request -> {
            Trip trip = tripRepository.findById(request.getTripId()).orElse(null);
            return mapAdvancePageDTO(request, null, null, null, trip);
        };
    }

    private int getOffset(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber can not be less than 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize can not be less than 1");
        }
        return ((pageNumber - 1) * pageSize);
    }
}
