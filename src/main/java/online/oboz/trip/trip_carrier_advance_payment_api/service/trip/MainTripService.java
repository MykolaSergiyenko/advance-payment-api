package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.CostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict.CostDictService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MainTripService implements TripService {
    private static final Logger log = LoggerFactory.getLogger(MainTripService.class);

    private final String tripCostError = "Задайте стоимость заказа поставщика.";
    private final String gt = "больше минимальной";
    private final String lt = "меньше максимальной";
    private final String costTitle = "Стоимость заказа поставщика должна быть %s стоимости в справочнике - %s руб.";
    private final String contactsTitle = "Не указаны контакты контрагента в разделе «Авансирование».";
    private final String docsTitle = "Загрузите 'Договор-заявку' или 'Заявку' в разделе «Документы».";


    private final TripRepository tripRepository;
    private final CostDictService costDictService;
    private final CostService costService;
    private final ContactService contactService;
    private final TripDocumentsService documentsService;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public MainTripService(
        TripRepository tripRepository,
        CostDictService costDictService,
        CostService costService,
        ContactService contactService, TripDocumentsService documentsService, ApplicationProperties applicationProperties
    ) {
        this.tripRepository = tripRepository;
        this.costDictService = costDictService;
        this.costService = costService;
        this.contactService = contactService;
        this.documentsService = documentsService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Trip findTripById(Long tripId) {
        log.info("--- findTripById: " + tripId);
        return tripRepository.findById(tripId).
            orElseThrow(() ->
                getTripsInternalError("Trip not found by id: " + tripId));
    }

    @Override
    public List<Trip> getAutoAdvanceTrips() {
        Double minCost = getTripMinCost();
        Double maxCost = getTripMaxCost();
        Long tripInterval = applicationProperties.getNewTripsInterval();
        OffsetDateTime minDate = OffsetDateTime.now().minusMinutes(tripInterval);
        log.info("--- Get auto-advance trips for minCost = {}; maxCost = {}; minDate = {}.",
            format(minCost), format(maxCost), minDate);
        return tripRepository.getTripsForAutoAdvance(minCost, maxCost, minDate);
    }

    @Override
    public Advance setSumsToAdvance(Advance advance, Trip trip) {
        try {
            TripCostInfo costInfo = new TripCostInfo(calculateTripCost(trip));
            advance.setCostInfo(costInfo);
            AdvanceCostDict dict = getAdvanceDictByCost(costInfo.getCost());
            AdvanceInfo info = new AdvanceInfo(dict.getAdvancePaymentSum(), dict.getRegistrationFee());
            advance.setTripAdvanceInfo(info);
        } catch (BusinessLogicException e) {
            log.error("Error while advance sums and costs calculation: {}.", e.getErrors());
        }
        return advance;
    }

    public Double getTripMinCost() {
        return costDictService.findMinCost();
    }

    public Double getTripMaxCost() {
        return costDictService.findMaxCost();
    }

    public AdvanceCostDict getAdvanceDictByCost(Double cost) {
        return costDictService.findAdvanceSumByCost(cost);
    }

    public Double calculateTripCost(Trip trip) {
        return costService.calculateWithNdsForTrip(trip);
    }

    public IsTripAdvanced checkTripAdvanceState(Trip trip) {
        IsTripAdvanced isTripAdvanced = new IsTripAdvanced();
        if (checkTripCosts(trip, isTripAdvanced)) {
            String message = "";
            if (contactService.notExistsByContractor(trip.getContractorId())) {
                message = contactsTitle;
            } else if (!documentsService.isAllTripDocumentsLoaded(trip.getId(), false)) {
                message = docsTitle;
            }
            isTripAdvanced.setTooltip(message);
        }
        return isTripAdvanced;
    }

    private Boolean checkTripCosts(Trip trip, IsTripAdvanced request) {
        Double minCost = getTripMinCost();
        Double maxCost = getTripMaxCost();
        try {
            Double ndsCost = calculateTripCost(trip);
            if (ndsCost < minCost || ndsCost > maxCost) {
                request.setTooltip(
                    (ndsCost < minCost) ? String.format(costTitle, gt, format(minCost)) :
                        ((ndsCost > maxCost) ? String.format(costTitle, lt, format(maxCost)) : null)
                );
                return false;
            } else {
                return true;
            }
        } catch (BusinessLogicException e) {
            request.setTooltip(tripCostError);
            return false;
        }
    }

    private String format(Double d) {
        return StringUtils.formatNum(d);
    }


    private BusinessLogicException getTripsInternalError(String message) {
        return ErrorUtils.getInternalError("Trip-service internal error: " + message);
    }
}
