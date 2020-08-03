package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.TripCostInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts.AdvanceCostDict;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs.CostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.costs.advancedict.CostDictService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.DateUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.TripAdvanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Сервис для работы с заказом поставщика-поездкой
 *
 * @author s‡oodent
 */
@Service
public class MainTripService implements TripService {
    private static final Logger log = LoggerFactory.getLogger(MainTripService.class);

    private final String tripNullCostError, costError, contactsError, docsError, gt, lt, datePattern;;

    private final TripRepository tripRepository;
    private final CostDictService costDictService;
    private final CostService costService;
    private final ContactService contactService;
    private final TripDocumentsService documentsService;
    private final Long interval;

    @Autowired
    public MainTripService(
        TripRepository tripRepository,
        CostDictService costDictService,
        CostService costService,
        ContactService contactService,
        TripDocumentsService documentsService,
        ApplicationProperties applicationProperties
    ) {
        this.tripRepository = tripRepository;
        this.costDictService = costDictService;
        this.costService = costService;
        this.contactService = contactService;
        this.documentsService = documentsService;
        this.interval = applicationProperties.getNewTripsInterval();
        this.tripNullCostError = applicationProperties.getNullCostError();
        this.gt = applicationProperties.getTripCostGt();
        this.lt = applicationProperties.getTripCostLt();
        this.costError = applicationProperties.getTripCostError();
        this.docsError = applicationProperties.getTripDocsError();
        this.contactsError = applicationProperties.getTripContractsError();
        this.datePattern = applicationProperties.getDatePattern();
    }

    @Override
    public Trip findTripById(Long tripId) {
        //log.info("--- findTripById: " + tripId);
        return tripRepository.findById(tripId).
            orElseThrow(() ->
                getTripsInternalError("Trip not found by id: " + tripId));
    }

    @Override
    public List<Trip> getAutoAdvanceTrips() {
        Double minCost = getTripMinCost();
        Double maxCost = getTripMaxCost();
        OffsetDateTime minDate = OffsetDateTime.now().minusMinutes(interval);
        log.info("[Auto-advance]: Get auto-advance trips by minDate: '{}'; cost-interval: {} - {}.",
            DateUtils.format(minDate, datePattern).trim(), formatCost(minCost), formatCost(maxCost));
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

    public TripAdvanceState checkTripAdvanceState(Trip trip) {
        String message = "";
        TripAdvanceState tripAdvanceState = new TripAdvanceState();
        if (checkTripCosts(trip, tripAdvanceState)) {
            if (contactService.notExistsByContractor(trip.getContractorId())) {
                message = contactsError;
            } else if (!documentsService.isAllTripDocumentsLoaded(trip.getId())) {
                message = docsError;
            }
            tripAdvanceState.setTooltip(message);
        }
        log.info("[Trip-advance]: Tooltip is: {}", tripAdvanceState.getTooltip());
        return tripAdvanceState;
    }

    private Boolean checkTripCosts(Trip trip, TripAdvanceState request) {
        Double minCost = getTripMinCost();
        Double maxCost = getTripMaxCost();
        if (null != trip.getTripCostInfo().getCost() && trip.getTripCostInfo().getCost() > 0.0) {
            try {
                Double ndsCost = calculateTripCost(trip);
                if (ndsCost < minCost || ndsCost > maxCost) {
                    request.setTooltip(
                        (ndsCost < minCost) ? String.format(costError, gt, formatCost(minCost)) :
                            ((ndsCost > maxCost) ? String.format(costError, lt, formatCost(maxCost)) : null)
                    );
                    return false;
                } else {
                    return true;
                }
            } catch (BusinessLogicException e) {
                request.setTooltip(tripNullCostError);
                return false;
            }
        } else {
            request.setTooltip(tripNullCostError);
            return false;
        }
    }


    private String formatCost(Double d) {
        return StringUtils.formatNum(d);
    }


    private BusinessLogicException getTripsInternalError(String message) {
        return ErrorUtils.getInternalError("Trip-service internal error: " + message);
    }
}
