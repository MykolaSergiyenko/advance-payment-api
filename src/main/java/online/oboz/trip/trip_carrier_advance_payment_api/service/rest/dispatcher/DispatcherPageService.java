package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.BasePersonService;

import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class DispatcherPageService implements DispatcherService {
    private static final Logger log = LoggerFactory.getLogger(DispatcherPageService.class);

    private final AdvanceService advanceService;
    private final BasePersonService personService;
    private final ContactService contactService;
    private final TripDocumentsService documentsService;


    @Autowired
    public DispatcherPageService(
        AdvanceService advanceService,
        BasePersonService personService,
        ContactService contactService,
        TripDocumentsService documentsService) {
        this.advanceService = advanceService;
        this.personService = personService;
        this.contactService = contactService;
        this.documentsService = documentsService;
    }

    @Override
    public ResponseEntity<Void> giveAdvanceForTrip(Long tripId) {
        Long authorId = SecurityUtils.getAuthPersonId();
        log.debug("--- Give advance request for trip: {} and author: {} ", tripId, authorId);
        Advance advance = advanceService.createAdvanceForTripAndAuthorId(tripId, authorId);
        log.info("Advance for tripId {} was created: {}", tripId, advance.getUuid());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IsTripAdvanced> isAdvanced(Long tripId) {
        log.debug("--- Advance state request for trip: {} and author: {} ", tripId);
        IsTripAdvanced isTripAdvanced = new IsTripAdvanced();
        Trip trip = advanceService.findTrip(tripId);
        Boolean advanceNotExists = advanceService.advancesNotExistsForTrip(trip);
        Boolean contactNotFound = contactService.notExistsByContractor(trip.getContractorId());
        isTripAdvanced.contactsNotFound(contactNotFound);
        isTripAdvanced.setIsButtonActive(advanceNotExists && !contactNotFound);
        isTripAdvanced.setTripDocsNotFound(!documentsService.isAllTripDocumentsLoaded(tripId));
        if (!advanceNotExists) {
            Advance advance = advanceService.findByTripId(tripId);
            personService.setAuthorInfo(isTripAdvanced, advance.getAuthorId());
            isTripAdvanced.setIsAutoRequested(advance.isAuto());
        }
        return new ResponseEntity<>(isTripAdvanced, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        log.debug("--- Get advance-contact request for contractor: {} ", contractorId);
        return contactService.getContactCarrier(contractorId);
    }

    @Override
    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        log.debug("--- Add advance-contact request: {} ", carrierContactDTO);
        return contactService.addContactCarrier(carrierContactDTO);
    }

    @Override
    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        log.debug("--- Update advance-contact request: {} ", carrierContactDTO);
        return contactService.updateContactCarrier(carrierContactDTO);
    }

}
