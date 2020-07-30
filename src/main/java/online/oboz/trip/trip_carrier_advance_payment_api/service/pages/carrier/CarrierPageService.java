package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.vats.VatService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments.AttachmentService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class CarrierPageService implements CarrierService {
    private static final Logger log = LoggerFactory.getLogger(CarrierPageService.class);

    private final AdvanceService advanceService;
    private final ContractorService advanceContractorService;
    private final AttachmentService attachmentService;

    private final List<String> zeroVats;


    private final AdvanceMapper advanceMapper = AdvanceMapper.advanceMapper;

    @Autowired
    public CarrierPageService(
        AdvanceService advanceService,
        ContractorService advanceContractorService,
        AttachmentService attachmentService,
        VatService vatService
    ) {
        this.advanceService = advanceService;
        this.advanceContractorService = advanceContractorService;
        this.attachmentService = attachmentService;
        this.zeroVats = vatService.getZeroCodes();
    }

    @Override
    public ResponseEntity<CarrierPage> getAdvance(UUID uuid) {
        Advance advance = advanceService.findByUuid(uuid);
        CarrierPage page = forCarrier(advance);
        advanceService.setRead(advance);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> carrierWantsAdvance(UUID uuid) {
        log.info("Got carrier 'I want yr Advance' request for uuid - {} ", uuid);
        return advanceService.setWantsAdvance(uuid);
    }

    @Override
    public ResponseEntity<Resource> downloadTemplate(UUID uuid) {
        log.info("Got download 'Advance-request'-template for carrier for advance:", uuid);
        return attachmentService.downloadTemplate(uuid);
    }

    @Override
    public ResponseEntity<Void> uploadAssignment(MultipartFile filename, Long id) {
        log.info("Got upload assignment from carrier request for adavnce: {} ", id);
        return attachmentService.uploadAssignment(filename, id);
    }


    // Mapping

    private CarrierPage forCarrier(Advance advance) {
        CarrierPage page = new CarrierPage();
        page = setCarrierPageAccessAndInfo(page, advance);
        return page;
    }

    private CarrierPage setCarrierPageAccessAndInfo(CarrierPage page, Advance advance) {
        // Carrier-Page is accessed while don't send
        if (null == advance.getUnfSentAt()) {
            try {
                Trip trip = advanceService.findTrip(advance.getAdvanceTripFields().getTripId());
                page = advanceMapper.toCarrierPage(advance, trip.getInfo());
                page.setIsVatPayer(advanceContractorService.isVatPayer(advance.getContractorId())
                    && !zeroVats.contains(trip.getVatCode()));
                page.setPageCarrierUrlIsAccess(true);
                return page;
            } catch (BusinessLogicException ex) {
                log.error("Error while find advance for CarrierPage: {} .", advance.getUuid() +
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
