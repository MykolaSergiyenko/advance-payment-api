package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.unf.UnfService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.Notificator;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.BasePersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.TripService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.DateUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class MainAdvanceService implements AdvanceService {
    private static final Logger log = LoggerFactory.getLogger(MainAdvanceService.class);

//    @Autowired
//    private StateMachine<CurrentAdvanceState, AdvanceEvent> stateMachine;

    private final String advanceTitle = "Аванс выдан: ";
    private final String autoTitle = " (в автоматическом режиме).";
    private final String authorTitle = ";\nАвтор: ";

    private final String AUTO_COMMENT;
    private final Person autoUser;
    private final Long interval;

    private final TripService tripService;
    private final BasePersonService personService;
    private final ContactService contactService;

    private final Notificator notificationService;
    private final TripDocumentsService documentsService;
    private final UnfService integration1cService;

    private final AdvanceRepository advanceRepository;

    private final AdvanceMapper mapper = AdvanceMapper.advanceMapper;

    @Autowired
    public MainAdvanceService(
        TripService tripService,
        BasePersonService personService,
        ContactService contactService,
        TripDocumentsService documentsService,
        Notificator notificationService,
        UnfService integration1cService,
        AdvanceRepository advanceRepository,
        @Value("${services.auto-advance-service.comment}") String autoComment,
        @Value("${services.auto-advance-service.newadvance-inteval}") Long advanceInterval
    ) {
        this.tripService = tripService;
        this.personService = personService;
        this.contactService = contactService;
        this.documentsService = documentsService;
        this.notificationService = notificationService;
        this.integration1cService = integration1cService;
        this.advanceRepository = advanceRepository;

        AUTO_COMMENT = autoComment;
        interval = advanceInterval;
        log.info("Auto-created-advance comment is: " + AUTO_COMMENT);
        log.info("Auto-advance interval: " + interval);
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
    public AdvanceDesktopDTO getAdvances(String tab, Filter filter) {
        int page = (filter.getPage() == null || filter.getPage() == 0) ? 1 : filter.getPage();
        int size = (filter.getPer() == null || filter.getPer() == 0) ? 1 : filter.getPer();
        SortBy sort = (filter.getSort() == null || filter.getSort().get(0) == null) ? new SortBy() : filter.getSort().get(0);
        return getAdvances(tab, page, size, sort);
    }


    private AdvanceDesktopDTO getAdvances(String tab, Integer pageNo, Integer pageSize, SortBy sortBy) {
        log.info("Get advances: tab = {}, page = {}, pageSize = {}, sortBy = {},{}.",
            tab, pageNo, pageSize, sortBy.getKey(), sortBy.getDir());
        return mapAdvancesToDesktop(
            getPage(tab, pageNo, pageSize, sortBy));
    }

    private Page<Advance> getPage(String tab, Integer pageNo, Integer pageSize, SortBy sortBy) {
        Sort.Direction dir = sortBy.getDir() == null ?
            Sort.Direction.ASC : Sort.Direction.fromString(sortBy.getDir().toString());

        SortByField sort = sortBy.getKey() == null ? SortByField.ID : sortBy.getKey();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, dir, sort.toString());
        log.info("--- Load tab: {}. Pageble: {}. ", tab, pageable);

        switch (tab) {
            case ("in_work"):
                return advanceRepository.findInWorkAdvances(pageable, AUTO_COMMENT);

            case ("problem"):
                return advanceRepository.findProblemAdvances(pageable, AUTO_COMMENT);

            case ("paid"):
                return advanceRepository.findPaidAdvances(pageable);

            case ("not_paid"):
                return advanceRepository.findNotPaidAdvances(pageable);

            case ("cancelled"):
                return advanceRepository.findCancelledAdvances(pageable);

            case ("all"):
            case ("none"):
                return advanceRepository.findAll(pageable);
            default:
                return null;
        }
    }

    private AdvanceDesktopDTO mapAdvancesToDesktop(Page<Advance> page) {
        AdvanceDesktopDTO desktop = new AdvanceDesktopDTO();
        List<AdvanceDTO> list = mapper.toAdvancesDTO(page == null ? null : page.stream().collect(Collectors.toList()));
        desktop.setAdvances(list);
        desktop.setPaginator(new Paginator().page(page.getPageable().getPageNumber()).
            per(page.getPageable().getPageSize()).total(page.getTotalElements()));
        log.info("--- Advances count: {}. Total elements page: {}.",
            (list == null ? '-' : list.size()), page.getTotalElements());
        return desktop;
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

//    @Override
//    public Advance findByTripNum(String tripNum) {
//        return advanceRepository.findByTripNum(tripNum).
//            orElseThrow(() ->
//                getAdvanceError("Advance not found by tripNum: " + tripNum));
//    }

    @Override
    public Advance findByTripId(Long tripId) {
        return advanceRepository.findByTripId(tripId).
            orElseThrow(() ->
                getAdvanceError("Advance not found by tripId: " + tripId));
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
            //stateMachine.sendEvent(AdvanceEvent.LOAD_DOCS);
        } else {
            throw getAdvanceError("Advance-application-file uuid can't be null.");
        }
        return advance;
    }


    @Override
    public ResponseEntity<Void> sendToUnfAdvance(Long advanceId) {
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
            log.info("Advance: {} - is confirmed.", advance.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Void> cancelAdvance(Long advanceId, String withComment) {
        try {
            Advance advance = findById(advanceId);
            if (!advance.isCancelled()) {
                advance.setCancelledComment(withComment);
                advance.setCancelled(true);
                saveAdvance(advance);
                //stateMachine.sendEvent(AdvanceEvent.CANCEL);
                log.info("Advance: {} - was cancelled - with comment: '{}'.", advance.getId(), withComment);
            } else {
                log.info("Advance: {} - is cancelled already.", advance.getId());
            }
        } catch (BusinessLogicException ex) {
            log.error("Advance cancellation is failed for id: " + advanceId + ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> changeComment(Long advanceId, String comment) {
        try {
            Advance advance = findById(advanceId);
            advance.setComment(comment);
            saveAdvance(advance);
            //stateMachine.sendEvent(AdvanceEvent.SET_COMMENT);
            log.info("Advance: {} - set comment - '{}'.", advanceId, comment);
        } catch (BusinessLogicException ex) {
            log.error("Advance: {} - comment changing failed: {} . Errors: {}", advanceId, comment, ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> setLoadingComplete(Long advanceId, Boolean loadingComplete) {
        try {
            Advance advance = findById(advanceId);
            advance.setLoadingComplete(loadingComplete);
            saveAdvance(advance);
            //if (loadingComplete) stateMachine.sendEvent(AdvanceEvent.COMPLETE_LOADING);
            log.info("Advance: {} - set loading-complete: '{}'", advance.getId(), loadingComplete);
        } catch (BusinessLogicException e) {
            log.error("Advance: {} 'loading-comlete'-setting failed failed. Errors: {}", advanceId, e.getErrors());
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
                    log.info("Auto-advance: {} was created for trip: {}.",
                        autoAdvance.getId(), autoAdvance.getAdvanceTripFields().getTripId());
                }
            } catch (Exception e) {
                log.info("Auto-advance error for trip: {}. Errors: ", trip.getId(), e.getMessage());
            }
        });
    }


    public TripAdvanceState checkAdvanceState(Advance advance) {
        TripAdvanceState advanceState = new TripAdvanceState();
        advanceState.setTooltip(advanceTitle + formatDateFront(advance.getCreatedAt()) +
            (advance.isAuto() ? autoTitle :
                (authorTitle + personService.getAuthorFullName(advance.getAuthorId())))
        );
        log.info("Advance-state info: {}", advanceState);
        return advanceState;
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
        advance = setFileUuidsFromTrip(trip.getId(), tripService.setSumsToAdvance(advance, trip));
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
            log.error("Trip-documents not found for tripId: {}. ", tripId);
        }
        return advance;
    }

    private Advance setRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = documentsService.getRequestUuidOrTripRequestUuid(attachments);
        setContractApplication(advance, uuid);
        return advance;
    }

//    private Advance setAssignmentRequestUuid(Advance advance, List<TripAttachment> attachments) {
//        UUID uuid = documentsService.getAssignmentRequestUuid(attachments);
//        setAdvanceApplication(advance, uuid);
//        return advance;
//    }


    private Advance setAdvanceApplicationFile(Advance advance, UUID uuid) {
        advance.setUuidAdvanceApplicationFile(uuid);
        saveAdvance(advance);
        return advance;
    }


    private void setWantsAdvance(Advance advance) {
        if (advance.getPushButtonAt() == null) {
            advance.setPushButtonAt(OffsetDateTime.now());
            saveAdvance(advance);
        }
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
        return advanceRepository.findUnreadAdvances(interval);
    }

    private String formatDateFront(OffsetDateTime date) {
        return DateUtils.format(date);
    }

    private BusinessLogicException getAdvanceError(String message) {
        return ErrorUtils.getInternalError("Advance-service internal error: " + message);
    }
}
