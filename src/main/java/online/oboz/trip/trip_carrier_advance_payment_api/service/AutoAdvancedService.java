package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;

import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@EnableScheduling
public class AutoAdvancedService {
    public static final String AUTO_ADVANCE_COMMENT = "Auto Created";

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripAdvanceRepository advanceRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final AdvanceContactRepository advanceContactRepository;

    private final ApplicationProperties applicationProperties;
    private final NotificationService notificationService;
    private final NewNotificationService newNotificationService;
    private final OrdersApiService ordersApiService;
    private final ExecutorService service = Executors.newFixedThreadPool(15);

    @Autowired
    public AutoAdvancedService(
        AdvancePaymentCostRepository advancePaymentCostRepository,
        TripAdvanceRepository advanceRepository,
        TripRepository tripRepository,
        ContractorRepository contractorRepository,
        AdvanceContactRepository advanceContactRepository,
        ApplicationProperties applicationProperties,
        NotificationService notificationService,
        NewNotificationService newNotificationService, OrdersApiService ordersApiService
    ) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.advanceRepository = advanceRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.advanceContactRepository = advanceContactRepository;
        this.applicationProperties = applicationProperties;
        this.notificationService = notificationService;
        this.newNotificationService = newNotificationService;
        this.ordersApiService = ordersApiService;
    }


    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    void updateFileUuid() {
        List<TripAdvance> advanceRequests = advanceRepository.findRequestsWithoutFiles();
        for (TripAdvance advanceRequest : advanceRequests) {
            Optional<Trip> trip = tripRepository.findById(advanceRequest.getTripId());
            if (!trip.isPresent()) {
                continue;
            }
            Map<String, String> fileUuidMap = ordersApiService.findTripRequestDocs(trip.get());
            if (!fileUuidMap.isEmpty()) {
                String fileContractRequestUuid = Optional
                    .ofNullable(fileUuidMap.get("request"))
                    .orElse(fileUuidMap.get("trip_request"));

                if (fileContractRequestUuid != null) {
                    advanceRequest.setIsDownloadedContractApplication(true);
                    advanceRequest.setUuidContractApplicationFile(fileContractRequestUuid);
                    log.info("UuidContractApplicationFile is: {}", advanceRequest.getUuidContractApplicationFile());
                }

                String fileAdvanceRequestUuid = fileUuidMap.get("assignment_advance_request");
                if (fileAdvanceRequestUuid != null) {
                    advanceRequest.setIsDownloadedContractApplication(true);
                    advanceRequest.setUuidAdvanceApplicationFile(fileAdvanceRequestUuid);
                    log.info("UuidAdvanceApplicationFile is: {}", advanceRequest.getUuidAdvanceApplicationFile());
                }

                if (advanceRequest.getUuidContractApplicationFile() != null &&
                    advanceRequest.getUuidAdvanceApplicationFile() != null) {
                    advanceRequest.setIs1CSendAllowed(true);
                    log.info("Is1CSendAllowed set true for advance: {}", advanceRequest);
                }
            }
        }
        advanceRepository.saveAll(advanceRequests);
    }

    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    void updateAutoAdvanceForContractors() {
        //minCountTrips.contractors.()
        List<Contractor> contractors = contractorRepository.getFullName(
            applicationProperties.getMinCountTrip(),
            applicationProperties.getMinDateTrip()
        );
        contractors.forEach(c -> {
            c.setIsAutoAdvancePayment(true);
            log.info("Contractor with id: {} IsAutoAdvancePayment.", c.getId());
        });
        contractorRepository.saveAll(contractors);
    }

    @Scheduled(cron = "${services.auto-advance-service.cron.creation}")
    void createTripRequestAdvancePayment() {
        //TODO: maximum concrete this query
      //Scheduled create autoadvance- and approved- motor-trips
        tripRepository.getAutoApprovedMotorTrips().forEach(trip -> {
                //TODO: use Trip, Costs, Contact checkUniq,
                // create auto-adcvance from trip, save
                // notificate for auto-advance
                TripAdvance advance = newAutoTripRequestAdvancePayment();
                    //trip
                    // set fields
                    advanceRepository.save(advance);

                newNotificationService.notificate(advance);

            });
//            //TODO: use Trip, Costs, Contact checkUniq,
//                Long tripId = trip.getId();
//                Double tripCostWithNds = trip.getCost(); // ндс?
//                AdvancePaymentCost advanceCost = advancePaymentCostRepository.getAdvancePaymentCost(tripCostWithNds);
//                log.info("start createTripRequestAdvancePayment for tripId: {}, with cost: {}",
//                    tripId, tripCostWithNds); // ндс?
//                if (advanceCost == null) {
//                    log.info("not found cost  for tripId: {} from advancePaymentCostRepository", tripId);
//                    return;
//                }
//                TripRequestAdvancePayment tripRequestAdvancePayment = newAutoTripRequestAdvancePayment();
//                log.info("tripRequestAdvancePayment for id : {}, is auto crated", tripRequestAdvancePayment.getId());
//                Long tripContractorId = trip.getContractorId();
//                ContractorAdvancePaymentContact contact = advanceContactRepository.find(tripContractorId).orElse(null);
//
//                if (contact != null) {
//                    MessageDto messageDto = new MessageDto(applicationProperties, tripRequestAdvancePayment, contact);
//                    sendNotifications(messageDto);
//                } else {
//                    log.info("Contact not found for trip {}.", trip.getNum());
//                }
//            }
//        );
   }


    private TripAdvance newAutoTripRequestAdvancePayment() {
        TripAdvance tripAdvance = new TripAdvance();
        tripAdvance.setComment(AUTO_ADVANCE_COMMENT); //TODO: drom app.properties
        advanceRepository.save(tripAdvance);
        return tripAdvance;
    }


}
