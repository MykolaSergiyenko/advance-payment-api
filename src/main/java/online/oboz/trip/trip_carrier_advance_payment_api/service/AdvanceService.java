package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.order.Order;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.FileAttachmentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.Integration1cService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;


@Service
public class AdvanceService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceService.class);

    private final NewNotificationService newNotificationService;
    private final Integration1cService integration1cService;
    private final FileAttachmentsService fileAttachmentsService;


    private final ApplicationProperties applicationProperties;

    private final PersonRepository personRepository;
    private final AdvanceRepository advanceRepository;
    private final TripRepository tripRepository;
    private final AdvanceContactsBookRepository advanceContactsBookRepository;

    private final AdvanceMapper mapper = AdvanceMapper.INSTANCE;

    public AdvanceService(NewNotificationService newNotificationService, Integration1cService integration1cService, FileAttachmentsService fileAttachmentsService, PersonRepository personRepository, AdvanceContactsBookRepository advanceContactsBookRepository,
                          ApplicationProperties applicationProperties, AdvanceRepository advanceRepository, TripRepository tripRepository) {
        this.newNotificationService = newNotificationService;
        this.integration1cService = integration1cService;
        this.fileAttachmentsService = fileAttachmentsService;
        this.personRepository = personRepository;
        this.advanceContactsBookRepository = advanceContactsBookRepository;
        this.applicationProperties = applicationProperties;

        this.advanceRepository = advanceRepository;
        this.tripRepository = tripRepository;
    }


    private List<Trip> getAutoAdvanceTrips() {
        Double minCost = applicationProperties.getMinTripCost();
        return tripRepository.getTripsForAutoAdvance(minCost);
    }


    public void giveAutoAdvances() {
        Person autoAuthor = getAdvanceSystemUser();
        List<Trip> autoTrips = getAutoAdvanceTrips();
        autoTrips.forEach(trip -> {
            try {
                long contractorId = trip.getContractorId();
                AdvanceContactsBook contact = advanceContactsBookRepository.findByContractorId(contractorId).
                    orElseThrow(() ->
                        getAdvanceInternalError("Advance contact for Trip not found: contractorId is " + contractorId));

                Advance autoAdvance = createAdvanceForTrip(trip, autoAuthor, contact);
                autoAdvance.setIsAuto(true);
                autoAdvance.setComment(applicationProperties.getAutoCreatedComment());
                advanceRepository.save(autoAdvance);

                if (autoAdvance.getContact() != null) {
                    newNotificationService.notify(autoAdvance);
                }

            } catch (Exception e) {
                log.info("Auto-advance error for trip:" + trip.getId() + ". Error:" + e.getMessage());
            }
        });
    }

    public Boolean isAllDocumentsLoaded(Advance advance) {
        return fileAttachmentsService.isDownloadAllDocuments(advance).getBody();
    }

    public void updateFileUuids() {
        List<Advance> advanceRequests = findForUpdateFilesUuids();
        advanceRequests.forEach(advance ->
        {
            advance = fileAttachmentsService.updateAttachmentsUuids(advance).getBody();
            saveAdvance(advance);
        });
    }

    public ResponseEntity<Void> confirmAdvance(Long advanceId) {
        Advance advance = advanceRepository.findByTripId(advanceId).orElseThrow(() ->
            getAdvanceInternalError("Trip advance not found")
        );

        // TODO: Test can be nullable?
        Trip trip = tripRepository.findById(advance.getAdvanceTripFields().getTripId()).orElseThrow(() ->
            getAdvanceInternalError("trip not found")
        );

        // TODO: Test can be nullable?
//        Order order = orderRepository.findById(trip.getTripFields().getOrderId()).orElseThrow(() ->
//            getAdvanceInternalError("order not found")
//        );

        boolean downloadAllDocuments = isAllDocumentsLoaded(advance);
        Boolean isCancelled = advance.isCancelled();
        if (downloadAllDocuments && !advance.isPushedUnfButton() && !isCancelled) {

            //TODO: Интеграция с 1с-УНФ?
            integration1cService.send1cNotification(advanceId);

            //Where is it set at default
            advance.setPushedUnfButton(true);
            advance.setIs1CSendAllowed(false);
            advance.setCarrierPageAccess(false);

            //what we update in trip-db?
            tripRepository.save(trip);
            advanceRepository.save(advance);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        if (!downloadAllDocuments) {
            throw getAdvanceInternalError("no download All Documents");
        } else if (isCancelled) {
            throw getAdvanceInternalError("isCancelled is true");
        } else {
            throw getAdvanceInternalError("unf already send");
        }
    }


    public ResponseEntity<Void> cancelAdvancePayment(Long tripId, String withComment) {
        try {
            Advance entity = findByTripId(tripId);
            if (!entity.isCancelled()) {
                entity.setCancelledComment(withComment);
                entity.setCancelled(true);
                advanceRepository.save(entity);
                log.info(" Advance for tripId {} was cancelled with comment: {}", tripId, withComment);
            }
        } catch (BusinessLogicException ex) {
            log.error("Advance cancel is failed for tripId: " + tripId + ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public CarrierPage forCarrier(Advance advance) {
        CarrierPage page = new CarrierPage();
        try {
            page = mapper.toCarrierPage(advance);
        } catch (BusinessLogicException ex) {
            log.error("Error while find advance for CarrierPage: " + advance.getUuid() +
                ". Errors:" + ex.getErrors());
        }
        setEmailRead(advance);
        return page;
    }

    public ResponseEntity<Void> changeAdvanceComment(AdvanceCommentDTO commentDTO) {
        try {
            Advance advance = mapper.setComment(commentDTO);
            advanceRepository.save(advance);
        } catch (BusinessLogicException ex) {
            log.error("Advance comment changing is failed for advance id: " + commentDTO.getId() +
                ". Errors:" + ex.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public ResponseEntity<Void> setLoadingComplete(Long tripId, Boolean loadingComplete) {
        try {
            Trip trip = findTripById(tripId);
            Advance advance = findByTripId(tripId);
            advance.setLoadingComplete(loadingComplete);
            advanceRepository.save(advance);
        } catch (BusinessLogicException e) {
            log.error("Advance 'loading-comlete' setting is failed for tripId: " + tripId + " Errors:" + e.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public List<Advance> findForUpdateFilesUuids() {
        return advanceRepository.findRequestsWithoutFiles();
    }


    public Advance createAdvanceForTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> getAdvanceInternalError("Trip not found"));
        AdvanceContactsBook contact = advanceContactsBookRepository.findByContractorId(trip.getContractorId()).
            orElseThrow(() -> getAdvanceInternalError("Advance-contact not found for tripId:" + tripId));
        Long authorId = SecurityUtils.getAuthPersonId();
        Person author = getPerson(authorId);
        Advance advance = createAdvanceForTrip(trip, author, contact);
        advanceRepository.save(advance);
        newNotificationService.notify(advance);
        return advance;
    }


//    public Advance createAdvanceByTables(Trip trip, Person author, AdvanceContactsBook contact){
//        Advance advance = new Advance();
//        advanceRepository.save(advance);
//        advance.setCostInfo(trip.getTripCostInfo());
//        advance.setAdvanceTripFields(trip.getTripFields());
//        advance.setContact(contact);
//        advance.setAuthor(author);
//        advance.setAuthorId(author.getId());
//        advanceRepository.save(advance);
//
//    }

    public Advance createAdvanceForTrip(Trip trip, Person author, AdvanceContactsBook contact) {
        //Advance advance = mapper.toAdvance(trip, autoAuthor, contact);
        //advance = new Advance(trip, autoAuthor, contact);
        Advance advance = new Advance();
        advance.setAuthor(author);
        advance.setAuthorId(author.getId());
        advance.setContractorId(trip.getContractorId());
        advance.setAdvanceTripFields(trip.getTripFields());
        advance.setCostInfo(trip.getTripCostInfo());
        advance.setTripAdvanceInfo(new AdvanceInfo());
        advance.setContact(contact);

        return advance;
    }

    private List<Advance> getAllAdvances(){
        return advanceRepository.findAll().stream()
            .collect(Collectors.toList());
    }

    ResponseEntity<AdvanceDesktopDTO> mapToDesktop( List<Advance> advances,  Filter filter){
        return new ResponseEntity<>(mapToAdvanceDesktop(advances, filter), HttpStatus.OK);
    }


    public ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter){
        List<Advance> all = getAllAdvances().stream()
            .filter(advance -> !advance.isProblem(applicationProperties.getAutoCreatedComment())
                && !advance.isPaid() && !advance.isCancelled()).collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter) {
        List<Advance> all = getAllAdvances().stream().
            filter(advance -> advance.isProblem(applicationProperties.getAutoCreatedComment())).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter) {
        List<Advance> all = getAllAdvances().stream().
            filter(advance -> advance.isPaid()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter) {
        List<Advance> all = getAllAdvances().stream().
            filter(advance -> !advance.isPaid()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter) {
        List<Advance> all = getAllAdvances().stream().
            filter(advance -> advance.isCancelled()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    private AdvanceDesktopDTO mapToAdvanceDesktop(List<Advance> advances, Filter filter){
        List<Advance> responseList = advances.stream()
            .skip(getOffset(filter.getPage(), filter.getPer()))
            .limit(filter.getPer())
            .collect(Collectors.toList());

        List<AdvanceDTO> dtoList = mapper.toAdvancesDTO(responseList);

        AdvanceDesktopDTO desktop = new AdvanceDesktopDTO();

        desktop.setAdvances(dtoList);

        desktop.setPaginator(
            new Paginator()
                .page(filter.getPage())
                .per(filter.getPer())
                .total(advances.size())
        );

        return desktop;
    }

    private int getOffset(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber can not be less than 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize can not be less than 1");
        }
        return ((pageNumber - 1) * pageSize);
    }

    public Advance findById(Long id) {
        return advanceRepository.findById(id).
            orElseThrow(() ->
                getAdvanceInternalError("Advance not found by id: " + id));
    }

    public Advance findByUuid(UUID uuid) {
        return advanceRepository.findByUuid(uuid).
            orElseThrow(() ->
                getAdvanceInternalError("Advance not found by uuid: " + uuid));
    }


    public Advance findByTripNum(String tripNum) {
        return advanceRepository.findByTripNum(tripNum).
            orElseThrow(() ->
                getAdvanceInternalError("Advance not found by tripNum: " + tripNum));
    }

    public Advance findByTripId(Long tripId) {
        return advanceRepository.findByTripId(tripId).
            orElseThrow(() ->
                getAdvanceInternalError("Advance not found by tripId: " + tripId));
    }

    public Trip findTripById(Long tripId) {
        return tripRepository.findById(tripId).
            orElseThrow(() ->
                getAdvanceInternalError("Trip not found by id: " + tripId));
    }

    public ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum) {
        log.info("Got downloadAvanceRequestTemplateForCarrier request tripNum - " + tripNum);
        return fileAttachmentsService.downloadAdvanceRequestTemplate(findByTripNum(tripNum));
    }

    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
        log.info("Got downloadAvanceRequestTemplate request tripNum - " + tripNum);
        return fileAttachmentsService.downloadAdvanceRequestTemplate(findByTripNum(tripNum));
    }


    public ResponseEntity<Resource> downloadAdvanceRequest(String tripNum) {
        log.info("Got downloadAvanseRequest request tripNum - " + tripNum);
        return fileAttachmentsService.downloadAdvanceRequest(findByTripNum(tripNum));
    }

    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        log.info("Got downloadRequest request tripNum - " + tripNum);
        return fileAttachmentsService.downloadRequest(findByTripNum(tripNum));
    }


    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAdvance request tripNum - " + tripNum);
        Advance advance = findByTripNum(tripNum);
        if (advance.isPushedUnfButton()) {
            throw getAdvanceInternalError("Uploading of 'request-advance'-file forbidden. " +
                "Send to UNF after docs uploaded.");
        }
        fileAttachmentsService.uploadRequestAdvance(advance, filename);
        saveAdvance(advance);
        return new ResponseEntity<>(OK);
    }


    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAvanceForCarrier request tripNum - " + tripNum);
        return uploadRequestAdvance(filename, tripNum);
    }


    public Person getAdvanceSystemUser() {
        return personRepository.findById(47700l).orElse(null);

    }

    public Person getPerson(Long id) {
        return personRepository.findById(id).
            orElseThrow(() -> getAdvanceInternalError("Author of advance not found."));
    }

    public Advance saveAdvance(Advance adv) {
        return advanceRepository.save(adv);
    }

    public void setEmailRead(Advance advance) {
        if (advance.getReadAt() == null) {
            advance.setReadAt(OffsetDateTime.now());
            advanceRepository.save(advance);
        }
    }

    public BusinessLogicException getServiceInternalError(String message, Class serviceClass) {
        return getInternalBusinessError(getServiceError(serviceClass.getName(), message), INTERNAL_SERVER_ERROR);
    }


    public BusinessLogicException getEntityServiceError(String message, Class serviceClass) {
        return getInternalBusinessError(getServiceError(serviceClass.getName(), message), UNPROCESSABLE_ENTITY);
    }

    private Error getServiceError(String className, String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("Business Error in class: '" +
            className + "' : " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }

    private BusinessLogicException getAdvanceInternalError(String message) {
        return getServiceInternalError(message, AdvanceService.class);
    }


}
