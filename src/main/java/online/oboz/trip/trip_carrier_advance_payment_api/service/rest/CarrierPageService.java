package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Location;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRequestRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.LocationRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.FrontAdvancePaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CarrierPageService {
    private static final Logger log = LoggerFactory.getLogger(CarrierPageService.class);

    private final AdvanceRequestRepository advanceRequestRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;

    public CarrierPageService(
        AdvanceRequestRepository advanceRequestRepository,
        TripRepository tripRepository,
        LocationRepository locationRepository
    ) {
        this.advanceRequestRepository = advanceRequestRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
    }

    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (t.getPageCarrierUrlIsAccess()) {
            frontAdvancePaymentResponse.setPushButtonAt(t.getPushButtonAt());
            frontAdvancePaymentResponse.setUrlAdvanceApplication(t.getUuidAdvanceApplicationFile());
            frontAdvancePaymentResponse.setTripCostWithVat(t.getTripCost());
            frontAdvancePaymentResponse.setAdvancePaymentSum(t.getAdvancePaymentSum());
            frontAdvancePaymentResponse.setRegistrationFee(t.getRegistrationFee());
            frontAdvancePaymentResponse.setIsCancelled(t.getIsCancelled());
            frontAdvancePaymentResponse.setIsPushedUnfButton(t.getIsPushedUnfButton());
            setTripInfo(frontAdvancePaymentResponse, t.getTripId());
            setEmailRead(t);
        } else {
            frontAdvancePaymentResponse.setPageCarrierUrlIsAccess(t.getPageCarrierUrlIsAccess());
            new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
            log.info("PageCarrierUrlIsAccess is false");
        }
        return new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
    }

    private void setEmailRead(TripRequestAdvancePayment advance) {
        if (!advance.getIsEmailRead()) {
            advance.setIsEmailRead(true);
            advance.setEmailReadAt(OffsetDateTime.now());
            advanceRequestRepository.save(advance);
        }
    }

    public ResponseEntity<Void> requestGiveAdvancePaymentForCarrier(UUID uuid) {
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            log.error("PageCarrierUrlIsAccess is false");
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null && !t.getIsCancelled()) {
            t.setPushButtonAt(OffsetDateTime.now());
            advanceRequestRepository.save(t);
            log.error("save ok for advance request with uuid: {} ,PushButtonAt: {}, CancelAdvance: {} ",
                uuid, t.getPushButtonAt(), t.getIsCancelled()
            );
        } else {
            log.error("Button already pushed or request was cancel");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void setTripInfo(FrontAdvancePaymentResponse frontAdvancePaymentResponse, Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElse(new Trip());
        setTripInfo(frontAdvancePaymentResponse, trip);
    }

    private void setTripInfo(FrontAdvancePaymentResponse frontAdvancePaymentResponse, Trip trip) {
        frontAdvancePaymentResponse.setTripNum(trip.getNum());
        TripInfo tripInfo = trip.getTripInfo();

        Location locOrigin = locationRepository.find(tripInfo.getOriginPlaceId()).orElse(new Location());
        frontAdvancePaymentResponse.setLoadingDate(tripInfo.getStartDate());
        frontAdvancePaymentResponse.setLoadingTz(locOrigin.getLocationTz());
        frontAdvancePaymentResponse.setFirstLoadingAddress(locOrigin.getAddress());

        Location locDest = locationRepository.find(tripInfo.getDestinationPlaceId()).orElse(new Location());
        frontAdvancePaymentResponse.setUnloadingDate(tripInfo.getEndDate());
        frontAdvancePaymentResponse.setUnloadingTz(locDest.getLocationTz());
        frontAdvancePaymentResponse.setLastUnloadingAddress(locDest.getAddress());
    }

    private TripRequestAdvancePayment getTripRequestAdvancePaymentByUUID(UUID uuid) {
        return advanceRequestRepository.find(uuid).orElseThrow(() ->
            getBusinessLogicException("AdvancePaymentRequest not found")
        );
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
