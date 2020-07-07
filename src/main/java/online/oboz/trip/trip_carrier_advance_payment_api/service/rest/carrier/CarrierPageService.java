package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats.VatService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarrierPageService {
    private static final Logger log = LoggerFactory.getLogger(CarrierPageService.class);

    private final AdvanceService advanceService;
    private final ContractorService advanceContractorService;
    private final VatService vatService;
    private final List<String> zeroVats;


    private final AdvanceMapper advanceMapper = AdvanceMapper.INSTANCE;


    public CarrierPageService(
        AdvanceService advanceService,
        ContractorService advanceContractorService,
        VatService vatService
    ) {
        this.advanceService = advanceService;
        this.advanceContractorService = advanceContractorService;
        this.vatService = vatService;
        this.zeroVats = vatService.getZeroCodes();
    }

    public ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid) {
        Advance advance = advanceService.findByUuid(uuid);
        CarrierPage page = forCarrier(advance);
        advanceService.setEmailRead(advance);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    private CarrierPage forCarrier(Advance advance) {
        CarrierPage page = new CarrierPage();
        page = setCarrierPageAccessAndInfo(page, advance);
        return page;
    }

    private CarrierPage setCarrierPageAccessAndInfo(CarrierPage page, Advance advance) {
        if (null == advance.is1CSendAllowed() || advance.is1CSendAllowed() == true) {
            try {
                Trip trip = advanceService.findTrip(advance.getAdvanceTripFields().getTripId());
                page = advanceMapper.toCarrierPage(advance, trip.getInfo());
                page.setIsVatPayer(advanceContractorService.isVatPayer(advance.getContractorId())
                    && !zeroVats.contains(trip.getVatCode()));
                page.setPageCarrierUrlIsAccess(true);
                return page;
            } catch (BusinessLogicException ex) {
                log.error("Error while find advance for CarrierPage: " + advance.getUuid() +
                    ". Errors:" + ex.getErrors());
            }
        } else {
            page.setPageCarrierUrlIsAccess(false);
        }
        return page;
    }


    public ResponseEntity<Void> carrierWantsAdvance(UUID uuid) {
        Advance a = advanceService.findByUuid(uuid);
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


    private BusinessLogicException getCarrierException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
