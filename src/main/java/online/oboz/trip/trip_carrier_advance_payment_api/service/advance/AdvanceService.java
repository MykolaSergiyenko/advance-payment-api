package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.CostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.ordersapi.OrdersFilesService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.unf.UnfService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.Notificator;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.BasePersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.BaseTripService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;


@Service
public class AdvanceService implements BaseAdvanceService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceService.class);

    private final String AUTO_COMMENT;

    private final BaseTripService tripService;
    private final BasePersonService personService;
    private final ContactService contactService;
    private final CostService costService;
    private final OrdersFilesService ordersFilesService;
    private final Notificator notificationService;
    private final UnfService integration1cService;

    private final ApplicationProperties applicationProperties;
    private final AdvanceRepository advanceRepository;

    public AdvanceService(
        BaseTripService tripService,
        BasePersonService personService,
        ContactService contactService,
        CostService costService,

        OrdersFilesService ordersFilesService,
        Notificator notificationService,
        UnfService integration1cService,
        ApplicationProperties applicationProperties,
        AdvanceRepository advanceRepository
    ) {
        this.tripService = tripService;
        this.personService = personService;
        this.contactService = contactService;
        this.costService = costService;

        this.ordersFilesService = ordersFilesService;
        this.notificationService = notificationService;
        this.integration1cService = integration1cService;

        this.advanceRepository = advanceRepository;
        this.applicationProperties = applicationProperties;

        AUTO_COMMENT = applicationProperties.getAutoCreatedComment();
    }

    @Override
    public Trip findTrip(Long tripId) {
        return tripService.findTripById(tripId);
    }


    @Override
    public Advance createAdvanceForTripAndAuthorId(Long tripId, Long authorId) {
        Trip trip = findTrip(tripId);
        if (advancesNotExistsForTrip(trip)) {
            Person author = personService.getPerson(authorId);
            Advance advance = newAdvanceForTripAndAuthor(trip, author);
            saveAdvance(advance);
            notifyAboutAdvance(advance);
            return advance;
        } else {
            throw getAdvanceError("Advance for trip id " + tripId + " already exists.");
        }
    }


    @Override
    public List<Advance> getAllAdvances() {
        return advanceRepository.findAll().stream().collect(Collectors.toList());
    }

    @Override
    public Advance findById(Long id) {
        return advanceRepository.findById(id).
            orElseThrow(() ->
                getAdvanceError("Advance not found by id: " + id));
    }

    @Override
    public Advance findByUuid(UUID uuid) {
        return advanceRepository.findByUuid(uuid).
            orElseThrow(() ->
                getAdvanceError("Advance not found by uuid: " + uuid));
    }

    @Override
    public Advance findByTripNum(String tripNum) {
        return advanceRepository.findByTripNum(tripNum).
            orElseThrow(() ->
                getAdvanceError("Advance not found by tripNum: " + tripNum));
    }

    @Override
    public Advance findByTripId(Long tripId) {
        return advanceRepository.findByTripId(tripId).
            orElseThrow(() ->
                getAdvanceError("Advance not found by tripId: " + tripId));
    }

    @Override
    public List<Advance> findAdvancesWithoutFiles() {
        return advanceRepository.findRequestsWithoutFiles();
    }


    @Override
    public Advance saveAdvance(Advance adv) {
        return advanceRepository.save(adv);
    }

    @Override
    public List<Advance> saveAll(List<Advance> adv) {
        return advanceRepository.saveAll(adv);
    }

    @Override
    public void notifyAboutAdvance(Advance advance) {
        notificationService.notify(advance);
        if (advance.getEmailSentAt() != null || advance.getSmsSentAt() != null) {
            setNotifiable(advance, true);
            advance.setNotifiedAt(OffsetDateTime.now());
        } else {
            setNotifiable(advance, false);
        }
        saveAdvance(advance);
    }

    @Override
    public void notifyAboutAdvanceScheduled(Advance advance) {
        notificationService.scheduledNotify(advance);
        if (advance.getEmailSentAt() != null || advance.getSmsSentAt() != null) {
            setNotifiable(advance, true);
            advance.setNotifiedDelayedAt(OffsetDateTime.now());
        } else {
            setNotifiable(advance, false);
        }
        saveAdvance(advance);
    }

    @Override
    public void notifyUnread() {
        List<Advance> advances = findUnreadAdvances();
        advances.forEach(advance -> {
            notifyAboutAdvanceScheduled(advance);
        });
    }


    @Override
    public void setEmailRead(Advance advance) {
        if (advance.getReadAt() == null) {
            advance.setReadAt(OffsetDateTime.now());
            saveAdvance(advance);
        }
    }


    @Override
    public ResponseEntity<Void> confirmAdvance(Long advanceId) {
        Advance advance = findById(advanceId);

        //Trip trip = tripService.findTripById(advance.getAdvanceTripFields().getTripId());
        //Order order = orderRepository.findById(trip.getTripFields().getOrderId()).orElseThrow(() ->
        //getAdvanceInternalError("order not found") );

        Boolean downloadAllDocuments = ordersFilesService.isDownloadAllDocuments(advance);
        Boolean isCancelled = advance.isCancelled();
        if (downloadAllDocuments && !advance.isPushedUnfButton() && !isCancelled) {
            //TODO: Интеграция с 1с-УНФ?
            integration1cService.send1cNotification(advanceId);

            //Where is it set at default
            //advance.setPushedUnfButton(true);
            advance.setIs1CSendAllowed(false);
            //advance.setCarrierPageAccess(false);

            saveAdvance(advance);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        if (!downloadAllDocuments) {
            throw getAdvanceError("no download All Documents");
        } else if (isCancelled) {
            throw getAdvanceError("isCancelled is true");
        } else {
            throw getAdvanceError("unf already send");
        }
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long tripId, String withComment) {
        try {
            Advance advance = findByTripId(tripId);
            setCancelled(advance, withComment);
        } catch (BusinessLogicException ex) {
            log.error("Advance cancel is failed for tripId: " + tripId + ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> changeAdvanceComment(AdvanceCommentDTO commentDTO) {
        try {
            setComment(commentDTO);
        } catch (BusinessLogicException ex) {
            log.error("Advance comment changing is failed for advance id: " + commentDTO.getId() +
                ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> setLoadingComplete(Long id, Boolean loadingComplete) {
        try {
            Advance advance = findById(id);
            setCompleteLoading(advance, loadingComplete);
        } catch (BusinessLogicException e) {
            log.error("Advance 'loading-comlete' setting is failed for advance: " + id + " Errors:" + e.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public void giveAutoAdvances(Person autoUser) {
        List<Trip> autoTrips = tripService.getAutoAdvanceTrips();
        autoTrips.forEach(trip -> {
            try {
                createAutoAdvanceForTrip(trip, autoUser);
            } catch (Exception e) {
                log.info("Auto-advance error for trip: " + trip.getId() + ". Error:" + e.getMessage());
            }
        });
    }


    private Advance createAutoAdvanceForTrip(Trip trip, Person autoUser) {
        Advance autoAdvance = newAdvanceForTripAndAuthor(trip, autoUser);
        autoAdvance.setIsAuto(true);
        autoAdvance.setComment(AUTO_COMMENT);
        log.info("Auto-created advance's comment is: " + AUTO_COMMENT);
        saveAdvance(autoAdvance);
        notifyAboutAdvance(autoAdvance);
        return autoAdvance;
    }

    private Advance newAdvanceForTripAndAuthor(Trip trip, Person author) {
        Long contractorId = trip.getContractorId();
        AdvanceContactsBook contact = contactService.findByContractor(contractorId);

        Advance advance = new Advance(author);
        advance.setContractorId(trip.getContractorId());
        advance.setAdvanceTripFields(trip.getTripFields());
        advance.setContact(contact);
        costService.setSumsToAdvance(advance, trip);
        return advance;
    }


    private Advance setComment(AdvanceCommentDTO commentDTO) {
        Advance advance = findById(commentDTO.getId());
        advance.setComment(commentDTO.getAdvanceComment());
        return saveAdvance(advance);
    }

    private Advance setCompleteLoading(Advance advance, Boolean loadingComplete) {
        advance.setLoadingComplete(loadingComplete);
        return saveAdvance(advance);
    }

    private Advance setCancelled(Advance advance, String comment) {
        if (!advance.isCancelled()) {
            advance.setCancelledComment(comment);
            advance.setCancelled(true);
            advance = saveAdvance(advance);
            log.info(" Advance for tripId {} was cancelled with comment: {}",
                advance.getAdvanceTripFields().getTripId(), comment);
        }
        return advance;
    }


    private void setNotifiable(Advance advance, Boolean value) {
        advance.setNotifiableAdvance(value);
        saveAdvance(advance);
    }

    private Boolean advanceExists(Long tripId, Long contractorId, Long driverId, Long orderId, String tripNum) {
        return advanceRepository.existsByIds(tripId, contractorId, driverId, orderId, tripNum);
    }

    private Boolean advancesNotExistsForTrip(Trip trip) {
        return !(advanceExists(trip.getId(),
            trip.getContractorId(),
            trip.getTripFields().getDriverId(),
            trip.getTripFields().getOrderId(),
            trip.getTripFields().getNum()));
    }


    private List<Advance> findUnreadAdvances() {
        return advanceRepository.findUnreadAdvances(applicationProperties.getSmsInterval());
    }


    private BusinessLogicException getAdvanceError(String message) {
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }


    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("Business Error in Advance-Service: '" + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }
}
