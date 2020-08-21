package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.DetailedPersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.unf.UnfService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.Notificator;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.persons.BasePersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.trip.TripService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.DateUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Сервис "Авансирование"
 */
@Service
public class BaseAdvanceService implements AdvanceService {
    private static final Logger log = LoggerFactory.getLogger(BaseAdvanceService.class);

//    @Autowired
//    private StateMachine<CurrentAdvanceState, AdvanceEvent> stateMachine;

    private final String advanceTitle;
    private final String autoTitle;
    private final String authorTitle;

    private final String AUTO_COMMENT;
    private final String datePattern;
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
    public BaseAdvanceService(
        TripService tripService,
        BasePersonService personService,
        ContactService contactService,
        TripDocumentsService documentsService,
        Notificator notificationService,
        UnfService integration1cService,
        AdvanceRepository advanceRepository,
        ApplicationProperties properties
    ) {
        this.tripService = tripService;
        this.personService = personService;
        this.contactService = contactService;
        this.documentsService = documentsService;
        this.notificationService = notificationService;
        this.integration1cService = integration1cService;
        this.advanceRepository = advanceRepository;

        AUTO_COMMENT = properties.getAutoCreatedComment();
        interval = properties.getNewTripsInterval();
        autoUser = personService.getAdvanceSystemUser();

        log.info("Init... [Авто-аванс]: технический пользователь: {}.", autoUser.getInfo().getFullName());
        log.info("Init... [Авто-аванс]: комментарий: '{}'.", AUTO_COMMENT);
        log.info("Init... [Авто-аванс]: интервал: {} минут.", interval);

        advanceTitle = properties.getAdvanceTitle();
        autoTitle = properties.getAutoTitle();
        authorTitle = properties.getAuthorTitle();
        datePattern = properties.getDatePattern();
    }

    @Override
    public Trip findTrip(Long tripId) {
        return tripService.findTripById(tripId);
    }

    @Override
    public Trip findAdvancedTrip(Long tripId) {
        return tripService.findAdvancedTripById(tripId);
    }


    @Override
    public Advance createAdvanceForTripAndAuthorId(Long tripId, Long authorId) {
        log.info("[Аванс]: запрос на создание аванса от пользователя {} для поездки {}.", authorId, tripId);
        Trip trip = findAdvancedTrip(tripId);
        if (advancesNotExistsForTrip(trip)) {
            Person author = personService.getPerson(authorId);
            DetailedPersonInfo info = author.getInfo();
//            log.info("[Аванс]: автор аванса: {}.", String.join(" ",
//                info.getFirstName(), info.getMiddleName(), info.getLastName()));
            Advance advance = newAdvanceForTripAndAuthor(trip, author);
            saveAdvance(advance);
            notifyAboutAdvance(advance);
            log.info("[Аванс]: uuid = '{}' создан пользователем '{}'.",
                advance.getUuid(), advance.getAuthor().getInfo().getEmail());
            return advance;
        } else {
            throw getAdvanceError("Аванс по заказу " + tripId + " уже создан.");
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
            log.info("[Аванс] по заказу '{}' уже создан.", tripId);
        } else {
            log.info("[Аванс] по заказу '{}' еще не создан.", tripId);
        }
    }


    @Override
    public List<Advance> getAllAdvances() {
        return advanceRepository.findAll().stream().collect(Collectors.toList());
    }

    @Override
    public AdvanceDesktopDTO getAdvances(String tab, Filter filter) {
        try {
            int page = (filter.getPage() == null || filter.getPage() == 0) ? 1 : filter.getPage();
            int size = (filter.getPer() == null || filter.getPer() == 0) ? 1 : filter.getPer();
            SortBy sort = (filter.getSort() == null || filter.getSort().size() == 0) ? null : filter.getSort().get(0);
            AdvanceDesktopDTO desktop = mapAdvancesToDesktop(getPage(tab, page, size, sort));
            logPaging(tab, page, size, sort, desktop);
            return desktop;
        } catch (Exception e) {
            log.error("[Рабочий стол авансирования]: Ошибка построения грида авансов - Фильтр: {}, вкладка: {}. Ошибки: {}.", filter, tab, e.getStackTrace());
            return null;
        }
    }

    private void logPaging(String tab, int page, int size, SortBy sort, AdvanceDesktopDTO desktop) {
        String sorting = (sort == null || sort.getKey() == null || sort.getKey().toString().isEmpty()) ?
            "-" : sort.getKey().toString();
        String total = (desktop == null || desktop.getPaginator() == null || desktop.getPaginator().getTotal() == null) ?
            "Ø" : desktop.getPaginator().getTotal().toString();
        log.info("[Рабочий стол авансирования]: Вкладка: '{}'; Всего авансов: {}; Страниц: '{} x {}'; Фильтр:'{}'.",
            tab, total, page, size, sorting);
    }


    private Page<Advance> getPage(String tab, Integer pageNo, Integer pageSize, SortBy sortBy) {
        Sort.Direction dir = (sortBy == null || sortBy.getDir() == null) ?
            Sort.Direction.ASC : Sort.Direction.fromString(sortBy.getDir().toString());
        SortByField sort = (sortBy == null || sortBy.getKey() == null) ? SortByField.ID : sortBy.getKey();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, dir, sort.toString());
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
        List<AdvanceDTO> list = mapper.toAdvancesDTO
            (page == null ? null : page.stream().collect(Collectors.toList()));
        desktop.setAdvances(list);
        desktop.setPaginator(new Paginator().page(page.getPageable().getPageNumber()).
            per(page.getPageable().getPageSize()).total(page.getTotalElements()));
        return desktop;
    }


    @Override
    public Advance findById(Long id) {
        return advanceRepository.findById(id).
            orElseThrow(() ->
                getAdvanceError("[Advance]: not found by id: " + id));
    }

    @Override
    public Advance findByUuid(UUID uuid) {
        return advanceRepository.findByUuid(uuid).
            orElseThrow(() ->
                getAdvanceError("[Advance]: not found by uuid: " + uuid));
    }


    @Override
    public Advance findByTripId(Long tripId) {
        return advanceRepository.findActiveByTripId(tripId).
            orElseThrow(() ->
                getAdvanceError("[Advance]: active advances not found by tripId: " + tripId));
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
        advance.setNotifiedAt(OffsetDateTime.now());
        saveAdvance(advance);
    }


    @Override
    public void notifyUnread() {
        List<Advance> advances = findUnreadAdvances();
        int unreadCount = advances.size();
        if (unreadCount > 0) {
            int contractorsSize = advances.stream().map(Advance::getContractorId).collect(Collectors.toSet()).size();
            log.info("[Авто-аванс]: Найдено {} 'непрочитанных' сообщений о создании аванса для {} разных контрагентов.",
                unreadCount, contractorsSize);
            notifyAboutAdvancesScheduled(advances);
        } else {
            log.info("[Авто-аванс]: 'Непрочитанных' сообщений о создании аванса не найдено.");
        }
    }


    @Override
    public void notifyAboutAdvancesScheduled(List<Advance> advances) {
        notificationService.repeatNotify(advances);
        saveAll(advances);
    }


    @Override
    public void setRead(Advance advance) {
        if (advance.getEmailSentAt() != null || advance.getSmsSentAt() != null) {
            if (advance.getReadAt() == null) {
                advance.setReadAt(OffsetDateTime.now());
                saveAdvance(advance);
                log.info("[Аванс]: сообщение об авансе прочитано: {}.", advance.getId());
            } else {
                log.info("[Аванс]: Сообщение уже прочитано по авансу: {}.", advance.getId());
            }
        } else {
            log.info("[Аванс]: Сообщения по авансу еще не отправлялись: {}.", advance.getId());
        }
    }

    @Override
    public ResponseEntity<Void> setWantsAdvance(UUID advanceUuid) {
        Advance advance = findByUuid(advanceUuid);
        setWantsAdvance(advance);
        log.info("[Аванс]: Нажата кнопка 'Хочу аванс' в {}.", advance.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public Advance setContractApplication(Advance advance, UUID uuid) {
        if (uuid != null) {
            advance.setUuidContractApplicationFile(uuid);
            saveAdvance(advance);
            log.info("[Аванс]: - {} - UUID 'Заявки' или 'Договор-заявки': {}.", advance.getId(),
                advance.getUuidContractApplicationFile());
        } else {
            throw getAdvanceError("[Аванс]: UUID 'Заявки' или 'Договор-заявки' не может быть пустым.");
        }
        return advance;
    }

    @Override
    public Advance setAdvanceApplication(Advance advance, UUID uuid) {
        if (uuid != null) {
            setAdvanceApplicationFile(advance, uuid);
            log.info("[Аванс]: - {} - UUID подписанной 'Заявки на аванс': {}.", advance.getId(),
                advance.getUuidAdvanceApplicationFile());
            //stateMachine.sendEvent(AdvanceEvent.LOAD_DOCS);
        } else {
            throw getAdvanceError("[Аванс]: UUID подписанной 'Заявки на аванс' не может быть пустым.");
        }
        return advance;
    }


    @Override
    public ResponseEntity<Void> sendToUnfAdvance(Long advanceId) {
        Advance advance = findById(advanceId);
        if (!documentsService.isAllDocumentsLoaded(advance)) {
            throw getAdvanceError("[Аванс]:" + advanceId + "- документы не загружены.");
        } else if (advance.isCancelled()) {
            throw getAdvanceError("[Аванс]:" + advanceId + " отменен.");
        } else if (advance.getUnfSentAt() != null) {
            throw getAdvanceError("[Аванс]:" + advanceId + " уже отправлен в УНФ.");
        } else {
            integration1cService.send1cNotification(advanceId);
            saveAdvance(advance);
            log.info("[Аванс]: {} - отправлен в УНФ.", advance.getId());
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
                advance.setCancelledAt(OffsetDateTime.now());
                saveAdvance(advance);
                //stateMachine.sendEvent(AdvanceEvent.CANCEL);
                log.info("[Аванс]: {} - отменен - комментарий: '{}'.", advance.getId(), withComment);
            } else {
                log.info("[Аванс]: {} - уже отменен.", advance.getId());
            }
        } catch (BusinessLogicException ex) {
            log.error("[Аванс]: ошибка при отмене: {}. Ошибки: {}.", advanceId, ex.getErrors());
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
            log.info("[Аванс]: {} - комментарий - '{}'.", advanceId, comment);
        } catch (BusinessLogicException ex) {
            log.error("[Аванс]: {} - ошибка при комментировании: {} . Ошибка: {}", advanceId, comment, ex.getErrors());
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
            log.info("[Аванс]: {} - водитель загружен: '{}'", advance.getId(), loadingComplete);
        } catch (BusinessLogicException e) {
            log.error("[Аванс]: {} 'загрузка завершена'. Ошибки: {}", advanceId, e.getErrors());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public void giveAutoAdvances() {
        List<Trip> autoTrips = tripService.getAutoAdvanceTrips();
        long size = autoTrips.size();
        long contractorsSize = autoTrips.stream().map(Trip::getContractorId).collect(Collectors.toSet()).size();
        if (size > 0) {
            log.info("[Авто-аванс]: Найдено {} поездок для выдачи 'авто-аванса' ( для {} разных контрагентов).",
                size, contractorsSize);

            autoTrips.forEach(trip -> {
                try {
                    log.info("[Авто-аванс]: создать аванс для поездки: {}.", trip.getId());

                    Advance autoAdvance = createAutoAdvanceForTrip(trip, autoUser);

                    if (null != autoAdvance) {
                        log.info("[Авто-аванс]: {} создан для поездки: {}.",
                            autoAdvance.getId(), autoAdvance.getAdvanceTripFields().getTripId());
                    }
                } catch (Exception e) {
                    log.info("[Авто-аванс]: Ошибка выдачи аванса по трипу: {}. Ошибки: ", trip.getId(), e.getMessage());
                }
            });
        } else log.info("[Авто-аванс]: Поездки для выдачи авто-аванса не найдены.");
    }

    @Override
    public void fixSums() {
        log.info("--- Пересчет сумм аванса ---");
        List<Advance> advances = advanceRepository.findAll();
        log.info("--- В системе «Обоза» найдено всего {} авансов.", advances.size());
        int counterCost = 0;
        int counterSum = 0;

        for (int i = 0; i < advances.size(); i++) {
            Advance advance = advances.get(i);

            try {
                Trip trip = findTrip(advance.getAdvanceTripFields().getTripId());
                Double factCost = advance.getCostInfo().getCost();
                Double calcCost = tripService.calculateTripCost(trip);

                if (!factCost.equals(calcCost)) {
                    log.info("В авансе [{} - {}] стоимость с НДС равна {}. Для поездки [№{} - {}] рассчетная стоимость с НДС: {}.",
                        advance.getId(), advance.getUuid(), factCost, trip.getTripFields().getNum(), trip.getId(), calcCost);

                    TripCostInfo newCost = new TripCostInfo();
                    newCost.setCost(calcCost);
                    advance.setCostInfo(newCost);
                    saveAdvance(advance);
                    counterCost++;

                    checkSumsByCost(advance, calcCost, factCost, counterSum);
                } else {
                    log.info("В авансе [{} - {} - {}] стоимость корректна {}.", advance.getId(), advance.getUuid(),
                        advance.getAdvanceTripFields().getNum(), factCost);
                    checkSumsByCost(advance, factCost, factCost, counterSum);
                }
            } catch (BusinessLogicException e) {
                log.error("Поездка не найдена по авансу {}.", advance.getId());
            }

        }

        log.info("--- Пересчет стоимости выполнен в {} авансах. Пересчет сумм и(или) сборов - в {} авансах.", counterCost, counterSum);
        log.info("--- Конец пересчета сумм аванса. Изменено как минимум {} из {} авансов.", counterSum, advances.size());
    }

    private void checkSumsByCost(Advance advance, Double newCost, Double oldCost, int counter) {
        AdvanceCostDict dict = tripService.getAdvanceDictByCost(newCost);
        Double dickSum = dict.getAdvancePaymentSum();
        Double dickFee = dict.getRegistrationFee();
        Double factSum = advance.getTripAdvanceInfo().getAdvancePaymentSum();
        Double factFee = advance.getTripAdvanceInfo().getRegistrationFee();

        if (!factSum.equals(dickSum) || !factFee.equals(dickFee)) {
            log.info("Аванс по заказу № {}, id = {}. Стоимость, сумма аванса и сбор [{} - {} - {}]. Для рассчитанной стоимости {} по справочнику сумм и сборов: [{} - {}].",
                advance.getAdvanceTripFields().getNum(), advance.getUuid(), oldCost, factSum, factFee, newCost, dickSum, dickFee);

            AdvanceInfo info = advance.getTripAdvanceInfo();
            info.setAdvancePaymentSum(dickSum);
            info.setRegistrationFee(dickFee);
            advance.setTripAdvanceInfo(info);

            saveAdvance(advance);
            counter++;
            log.info("Аванс [{} -{}] сохранен. Было [{} - {} - {}]. Стало [{} - {} - {}].", advance.getId(), advance.getUuid(),
                oldCost, factSum, factFee,
                advance.getCostInfo().getCost(),
                advance.getTripAdvanceInfo().getAdvancePaymentSum(),
                advance.getTripAdvanceInfo().getRegistrationFee());
        }
    }


    public TripAdvanceState checkAdvanceState(Advance advance) {
        TripAdvanceState advanceState = new TripAdvanceState();
        advanceState.setTooltip(advanceTitle + formatDateFront(advance.getCreatedAt()) +
            (advance.isAuto() ? autoTitle : (authorTitle + personService.getAuthorFullName(advance.getAuthorId()))));
        advanceState.setState(advance.getAdvanceState());
        log.info("[Состояние аванса]: состояние = '{}', подсказка = '{}'.", advanceState.getState(), advanceState.getTooltip());
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
            throw getAdvanceError("[Аванс] для поездки " + trip.getId() + " уже существует.");
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
            log.info("[Advance]: Found {} file-attachments in trip {} for advance.", advance.getId(), attachSize, tripId);
            if (attachSize > 0) {
                setRequestUuid(advance, attachments);
            }
        } catch (BusinessLogicException e) {
            log.error("[Аванс]: Не найдены документы для поездки: {}. ", tripId);
        }
        return advance;
    }

    private Advance setRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = documentsService.getRequestUuidOrTripRequestUuid(attachments);
        setContractApplication(advance, uuid);
        return advance;
    }


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
        return DateUtils.format(date, datePattern);
    }

    private BusinessLogicException getAdvanceError(String message) {
        return ErrorUtils.getInternalError("Advance-service internal error: " + message);
    }
}
