package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Location;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;
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

    private final TripAdvanceRepository tripAdvanceRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;

    public CarrierPageService(
        TripAdvanceRepository tripAdvanceRepository,
        TripRepository tripRepository,
        LocationRepository locationRepository
    ) {
        this.tripAdvanceRepository = tripAdvanceRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
    }

    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        TripAdvance tripAdvance = getTripRequestAdvancePaymentByUUID(uuid);
        if (tripAdvance.getPageCarrierUrlIsAccess()) {
            frontAdvancePaymentResponse.setPushButtonAt(
                tripAdvance.getPushButtonAt());
            frontAdvancePaymentResponse.setUrlAdvanceApplication(
                tripAdvance.getUuidAdvanceApplicationFile());
            frontAdvancePaymentResponse.setTripCostWithVat(
                tripAdvance.getTripCost());
            frontAdvancePaymentResponse.setAdvancePaymentSum(
                tripAdvance.getAdvancePaymentSum());
            frontAdvancePaymentResponse.setRegistrationFee(
                tripAdvance.getRegistrationFee());
            frontAdvancePaymentResponse.setIsCancelled(
                tripAdvance.getIsCancelled());
            frontAdvancePaymentResponse.setIsPushedUnfButton(
                tripAdvance.getIsPushedUnfButton());

            setTripInfo(frontAdvancePaymentResponse,
                tripAdvance.getTripId());

            //# Fact of e-mail note was read set here
            //  addresser open advance LK-url
            setEmailRead(tripAdvance);
        } else {
            frontAdvancePaymentResponse.setPageCarrierUrlIsAccess(tripAdvance.getPageCarrierUrlIsAccess());
            new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
            log.info("PageCarrierUrlIsAccess is false");
        }
        return new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
    }

    private void setEmailRead(TripAdvance advance) {
        if (!advance.getIsEmailRead()) {
            advance.setIsEmailRead(true);
            advance.setEmailReadAt(OffsetDateTime.now());
            tripAdvanceRepository.save(advance);
        }
    }

    public ResponseEntity<Void> requestGiveAdvancePaymentForCarrier(UUID uuid) {
        TripAdvance t = getTripRequestAdvancePaymentByUUID(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            log.error("PageCarrierUrlIsAccess is false");
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null && !t.getIsCancelled()) {
            t.setPushButtonAt(OffsetDateTime.now());
            tripAdvanceRepository.save(t);
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

    private TripAdvance getTripRequestAdvancePaymentByUUID(UUID uuid) {
        return tripAdvanceRepository.find(uuid).orElseThrow(() ->
            getBusinessLogicException("AdvancePaymentRequest not found")
        );
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
