package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.CostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.unf.UnfService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.Notificator;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.BasePersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.TripService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class MainAdvanceService implements AdvanceService {
    private static final Logger log = LoggerFactory.getLogger(MainAdvanceService.class);

    private final String AUTO_COMMENT;
    private final Person autoUser;

    private final TripService tripService;
    private final BasePersonService personService;
    private final ContactService contactService;
    private final CostService costService;

    private final Notificator notificationService;
    private final TripDocumentsService documentsService;
    private final UnfService integration1cService;

    private final ApplicationProperties applicationProperties;
    private final AdvanceRepository advanceRepository;

    public MainAdvanceService(
        TripService tripService,
        BasePersonService personService,
        ContactService contactService,
        CostService costService,
        TripDocumentsService documentsService,
        Notificator notificationService,
        UnfService integration1cService,
        ApplicationProperties applicationProperties,
        AdvanceRepository advanceRepository
    ) {
        this.tripService = tripService;
        this.personService = personService;
        this.contactService = contactService;
        this.costService = costService;
        this.documentsService = documentsService;
        this.notificationService = notificationService;
        this.integration1cService = integration1cService;
        this.advanceRepository = advanceRepository;
        this.applicationProperties = applicationProperties;

        AUTO_COMMENT = applicationProperties.getAutoCreatedComment();
        log.info("Auto-created-advance comment is: " + AUTO_COMMENT);
        autoUser = personService.getAdvanceSystemUser();
        log.info("Auto-advance system-user is: {}", autoUser.getInfo());
    }

    @Override
    public Trip findTrip(Long tripId) {
        return tripService.findTripById(tripId);
    }


    @Override
    public Advance createAdvanceForTripAndAuthorId(Long tripId, Long authorId) {
        log.info("Request for Advance-creation from {} for Trip {}.", authorId, tripId);
        Trip trip = findTrip(tripId);
        if (advancesNotExistsForTrip(trip)) {
            Person author = personService.getPerson(authorId);
            DetailedPersonInfo info = author.getInfo();
            log.info("Found advance Author-person: {}.", String.join(" ",
                info.getFirstName(), info.getMiddleName(), info.getLastName()));
            Advance advance = newAdvanceForTripAndAuthor(trip, author);
            saveAdvance(advance);
            notifyAboutAdvance(advance);
            log.info("Advance (uuid = {}) was created for author {}.",
                advance.getUuid(), advance.getAuthorId());
            return advance;
        } else {
            throw getAdvanceError("Advance for trip id " + tripId + " already exists.");
        }
    }

    @Override
    public Boolean advancesNotExistsForTrip(Trip trip) {
        Long tripId = trip.getId();
        Boolean exsts = advanceExists(tripId,
            trip.getContractorId(),
            trip.getTripFields().getDriverId(),
            trip.getTripFields().getOrderId(),
            trip.getTripFields().getNum());
        logExist(exsts, tripId);
        return !exsts;
    }

    private void logExist(boolean exsts, long tripId) {
        if (exsts) {
            log.info("Advance is exists already for Trip {}.", tripId);
        } else {
            log.info("Advance for Trip {} not exists yet.", tripId);
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
        log.info("Found 'unread' advances {}.", advances.size());
        advances.forEach(advance -> {
            notifyAboutAdvanceScheduled(advance);
        });
    }


    @Override
    public void setRead(Advance advance) {
        if (advance.getEmailSentAt() != null || advance.getSmsSentAt() != null) {
            if (advance.getReadAt() == null) {
                advance.setReadAt(OffsetDateTime.now());
                saveAdvance(advance);
                log.info("Set 'read-at' for advance: {}.", advance.getId());
            } else {
                log.info("Message is already 'read' for advance: {}.", advance.getId());
            }
        } else {
            log.info("Messages weren't been sent yet for advance: {}.", advance.getId());
        }
    }

    @Override
    public ResponseEntity<Void> setWantsAdvance(UUID advanceUuid) {
        Advance advance = findByUuid(advanceUuid);
        setWantsAdvance(advance);
        log.info("Update 'wants-advance' in {}.", advance.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public Advance setContractApplication(Advance advance, UUID uuid) {
        if (uuid != null) {
            advance.setUuidContractApplicationFile(uuid);
            saveAdvance(advance);
            log.info("Update contract-application-file in {}. Set uuid = {} ", advance.getId(),
                advance.getUuidContractApplicationFile());
        } else {
            throw getAdvanceError("Advance-contract-file uuid can't be null.");
        }
        return advance;
    }

    @Override
    public Advance setAdvanceApplication(Advance advance, UUID uuid) {
        if (uuid != null) {
            setAdvanceApplicationFile(advance, uuid);
            log.info("Update advance-application-file in {}. Set uuid = {} ", advance.getId(),
                advance.getUuidAdvanceApplicationFile());
        } else {
            throw getAdvanceError("Advance-application-file uuid can't be null.");
        }
        return advance;
    }


    @Override
    public ResponseEntity<Void> confirmAdvance(Long advanceId) {
        Advance advance = findById(advanceId);

        if (!documentsService.isAllDocumentsLoaded(advance)) {
            throw getAdvanceError("Not all documents are loaded for advance: " + advanceId);
        } else if (advance.isCancelled()) {
            throw getAdvanceError("Advance was cancelled: " + advanceId);
        } else if (advance.getUnfSentAt() != null) {
            throw getAdvanceError("Advance was already sent to UNF.");
        } else {
            integration1cService.send1cNotification(advanceId);
            saveAdvance(advance);
            log.info("Advance is confirmed {}.", advance.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long tripId, String withComment) {
        try {
            Advance advance = findByTripId(tripId);
            setCancelled(advance, withComment);
            log.info("Set cancelled with comment '{}' for advance {}.", withComment, advance.getId());
        } catch (BusinessLogicException ex) {
            log.error("Advance cancel is failed for tripId: " + tripId + ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> changeAdvanceComment(AdvanceCommentDTO commentDTO) {
        try {
            setComment(commentDTO);
            log.info("Set comment for advance = {}.", commentDTO.toString());
        } catch (BusinessLogicException ex) {
            log.error("Advance comment changing is failed for advance id: " + commentDTO.getId() +
                ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> setLoadingComplete(Long advanceId, Boolean loadingComplete) {
        try {
            Advance advance = findById(advanceId);
            setCompleteLoading(advance, loadingComplete);
            log.info("Loading-completed= {} set for advance {}.", loadingComplete, advance.getId());
        } catch (BusinessLogicException e) {
            log.error("Advance 'loading-comlete' setting is failed for advance: " + advanceId + " Errors:" + e.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public void giveAutoAdvances() {
        List<Trip> autoTrips = tripService.getAutoAdvanceTrips();
        long size = autoTrips.size();
        log.info("Found {} trips for auto-contractors.", size);
        if (size > 0) log.info("Auto-advance technical account: {}.", autoUser);
        autoTrips.forEach(trip -> {
            try {
                log.info("Try create auto-advance for trip {}.", trip.getId());

                Advance autoAdvance = createAutoAdvanceForTrip(trip, autoUser);

                if (null != autoAdvance) {
                    log.info("Auto-advance {} was created for trip {}.",
                        autoAdvance.getId(), autoAdvance.getAdvanceTripFields().getTripId());
                }
            } catch (Exception e) {
                log.info("Auto-advance error for trip: " + trip.getId() + ". Error:" + e.getMessage());
            }
        });
    }


    private Advance createAutoAdvanceForTrip(Trip trip, Person autoUser) {
        if (advancesNotExistsForTrip(trip)) {
            Advance autoAdvance = newAdvanceForTripAndAuthor(trip, autoUser);
            autoAdvance.setIsAuto(true);
            autoAdvance.setComment(AUTO_COMMENT);
            saveAdvance(autoAdvance);
            notifyAboutAdvance(autoAdvance);
            return autoAdvance;
        } else {
            throw getAdvanceError("Advance for trip id " + trip.getId() + " already exists.");
        }
    }

    private Advance newAdvanceForTripAndAuthor(Trip trip, Person author) {
        Long contractorId = trip.getContractorId();
        AdvanceContactsBook contact = contactService.findByContractor(contractorId);
        Advance advance = new Advance(author, trip, contact);
        advance = costService.setSumsToAdvance(advance, trip);
        advance = setFileUuidsFromTrip(trip.getId(), advance);
        return advance;
    }

    private Advance setFileUuidsFromTrip(Long tripId, Advance advance) {
        try {
            List<TripAttachment> attachments = documentsService.getTripAttachments(tripId);
            int attachSize = attachments.size();
            log.info("Found {} file-attachments in trip {} for advance {}.", attachSize, tripId, advance.getId());
            if (attachSize > 0) {
                setRequestUuid(advance, attachments);
                //setAssignmentRequestUuid(advance, attachments);
            }
        } catch (BusinessLogicException e) {
            log.error("Trip-documents not found for tripId = {}", tripId);
        }
        return advance;
    }

    private Advance setRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = documentsService.getRequestUuidOrTripRequestUuid(attachments);
        setContractApplication(advance, uuid);
        return advance;
    }

    private Advance setAssignmentRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = documentsService.getAssignmentRequestUuid(attachments);
        setAdvanceApplication(advance, uuid);
        return advance;
    }


    private Advance setAdvanceApplicationFile(Advance advance, UUID uuid) {
        advance.setUuidAdvanceApplicationFile(uuid);
        saveAdvance(advance);
        return advance;
    }

    private Advance setAllowedToSent(Advance advance) {
        if (advance.is1CSendAllowed()) {
            //advance.setIs1CSendAllowed(true);
            saveAdvance(advance);
            log.info("Set 's1CSendAllowed' for advance {}", advance.getId());
        }
        return advance;
    }


    private void setWantsAdvance(Advance advance) {
        if (advance.getPushButtonAt() == null) {
            advance.setPushButtonAt(OffsetDateTime.now());
            saveAdvance(advance);
        }
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

    // Здесь проверка на существование аванса
    // по "сложному внешнему ключу"
    // - все поля - проекция полей Трипа
    private Boolean advanceExists(Long tripId, Long contractorId, Long driverId, Long orderId, String tripNum) {
        checkEmpty(tripNum, tripId, contractorId, driverId, orderId);
        return advanceRepository.existsActualByIds(tripId, contractorId, driverId, orderId, tripNum);
    }

    private void checkEmpty(String num, Long... ids) {
        if (StringUtils.isEmptyLongs(ids) || StringUtils.isEmptyString(num)) {
            throw getAdvanceError("Empty params for advanceExists: " + StringUtils.getIds(ids) + "; num = " + num);
        }
    }


    private List<Advance> findUnreadAdvances() {
        return advanceRepository.findUnreadAdvances(applicationProperties.getNewTripsInterval());
    }

    private BusinessLogicException getAdvanceError(String message) {
        return ErrorUtils.getInternalError("Advance-service internal error: " + message);
    }
}
