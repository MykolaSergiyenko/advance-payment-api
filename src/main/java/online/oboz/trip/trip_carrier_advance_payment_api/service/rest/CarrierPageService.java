package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
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

    private final AdvanceService advanceService;


    public CarrierPageService(
        AdvanceService advanceService
    ) {
        this.advanceService = advanceService;
    }

    public ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid) {
        Advance advance = advanceService.findByUuid(uuid);
        CarrierPage page = advanceService.forCarrier(advance);
        if (!advance.isUnfSend()) {
            page.setPageCarrierUrlIsAccess(true);

        } else {
            page.setPageCarrierUrlIsAccess(false);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public ResponseEntity<Void> carrierWantsAdvance(UUID uuid) {
//        Advance t = getTripRequestAdvancePaymentByUUID(uuid);
//        if (!t.isCarrierPageAccess()) {
//            log.error("PageCarrierUrlIsAccess is false");
//            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
//        }
//        if (t.getPushButtonAt() == null && !t.isCancelled()) {
//            //t.setPushButtonAt(OffsetDateTime.now());
//            advanceRepository.save(t);
//            log.error("save ok for advance request with uuid: {} ,PushButtonAt: {}, CancelAdvance: {} ",
//                uuid, t.getPushButtonAt(), t.isCancelled()
//            );
//        } else {
//            log.error("Button already pushed or request was cancel");
//        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    private void setTripInfo(CarrierPage CarrierPage, Long tripId) {
//        Trip trip = tripRepository.findById(tripId).orElse(new Trip());
//        setTripInfo(CarrierPage, trip);
//    }

    private void setTripInfo(CarrierPage CarrierPage, Trip trip) {
        CarrierPage.setTripNum(trip.getTripFields().getNum());
        //TripInfo tripInfo = trip.getTripInfo();

//        List<CommonLocation> points = trip.getL();
//
//        CommonLocation locOrigin = tripInfo.getOriginLocation();
//        CommonLocation locDest = tripInfo.getDestLocation();

//        CarrierPage.setTripNum(trip.getTripNum());
//        TripInfo tripInfo = trip.getTripInfo();
//
//
//
//        CommonLocation locOrigin = locationRepository.find(tripInfo.getOriginPlaceId()).orElse(new CommonLocation());
//        CarrierPage.setLoadingDate(tripInfo.getStartDate());
//        CarrierPage.setLoadingTz(locOrigin.getLocationTz());
//        CarrierPage.setFirstLoadingAddress(locOrigin.getAddress());
//
//        CommonLocation locDest = locationRepository.find(tripInfo.getDestinationPlaceId()).orElse(new CommonLocation());
//        CarrierPage.setUnloadingDate(tripInfo.getEndDate());
//        CarrierPage.setUnloadingTz(locDest.getLocationTz());
//        CarrierPage.setLastUnloadingAddress(locDest.getAddress());
    }


    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
