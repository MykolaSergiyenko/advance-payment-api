package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats.VatService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarrierPageService implements CarrierService {
    private static final Logger log = LoggerFactory.getLogger(CarrierPageService.class);

    private final AdvanceService advanceService;
    private final ContractorService advanceContractorService;
    private final VatService vatService;
    private final List<String> zeroVats;


    private final AdvanceMapper advanceMapper = AdvanceMapper.advanceMapper;

    @Autowired
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

    @Override
    public ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid) {
        Advance advance = advanceService.findByUuid(uuid);
        CarrierPage page = forCarrier(advance);
        advanceService.setRead(advance);
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

    private BusinessLogicException getCarrierException(String s) {
        return ErrorUtils.getInternalError("Carrier-page error: " + s);
    }
}
