package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;

import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.BaseAdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.AdvanceContactService;


import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.CostService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.ordersapi.OrdersFilesService;

import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.unf.UnfService;

import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.BaseRestService;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
public class AdvanceServiceTest {

    private BaseRestService tripService;
    private BaseAdvanceService personService;
    private ContactService contactService;
    private CostService costService;
    private OrdersFilesService ordersFilesService;
    private Notificator notificationService;
    private UnfService integration1cService;

    private ApplicationProperties applicationProperties;

    private AdvanceRepository advanceRepository;

    private BaseAdvanceService advanceService;

    private AdvanceMapper mapper;


//
//    @BeforeEach
//    void initTest() {
//        System.out.println("--- init ---");
//
//        RestTemplate rest = new RestTemplate();
//
//        applicationProperties = new ApplicationProperties();
//
//
//
//        tripService = mock(TripService.class);
//        when(tripService.findTripById(any())).thenReturn(random(Trip.class));
//        when(tripService.getAutoAdvanceTrips()).thenReturn(randomListOf(100, Trip.class));
//
//
//        personService = mock(PersonService.class);
//        when(personService.getAdvanceSystemUser()).thenReturn(random(Person.class));
//        when(personService.getPerson(any())).thenReturn(random(Person.class));
//
//
//        contactService = mock(AdvanceContactService.class);
//        when(contactService.getContactCarrier(any())).thenReturn(random(ResponseEntity.class));
//        when(contactService.addContactCarrier(any())).thenReturn(random(ResponseEntity.class));
//        when(contactService.updateContactCarrier(any())).thenReturn(random(ResponseEntity.class));
//
//        costService = mock(AdvanceCostService.class);
//        when(costService.calculateNdsCost(any(), any(), any())).thenReturn(random(Double.class));
//        when(costService.setSumsToAdvance(any(), any())).thenReturn(random(Advance.class));
//
//        ordersFilesService = mock(OrdersApiService.class);
//        when(ordersFilesService.findAdvanceRequestDocs(any())).thenReturn(random(HashMap.class));
//        when(ordersFilesService.findTripRequestDocs(any())).thenReturn(random(HashMap.class));
//        when(ordersFilesService.findAllDocuments(any())).thenReturn(random(TripDocuments.class));
//        when(ordersFilesService.isDownloadAllDocuments(any())).thenReturn(random(Boolean.class));
//        when(ordersFilesService.saveTripDocuments(any(), any(), any())).thenReturn(random(Boolean.class));
//
//        notificationService = mock(NotificationService.class);
//        when(notificationService.notify(any())).thenReturn(random(Advance.class));
//        when(notificationService.scheduledNotify(any())).thenReturn(random(Advance.class));
//
//        integration1cService = mock(Integration1cService.class);
//        integration1cService.send1cNotification(any());
//
//        advanceRepository = mock(AdvanceRepository.class);
//        when(advanceRepository.findAll()).thenReturn(randomListOf(180, Advance.class));
//        when(advanceRepository.existsByIds(any(), any(), any(), any(), any())).thenReturn(random(Boolean.class));
//        when(advanceRepository.findByTripId(any())).thenReturn(random(Optional.class));
//        when(advanceRepository.findByTripNum(any())).thenReturn(random(Optional.class));
//        when(advanceRepository.findByUuid(any())).thenReturn(random(Optional.class));
//        when(advanceRepository.findRequestsWithoutFiles());
//        when(advanceRepository.findUnreadAdvances(60)).thenReturn(randomListOf(37, Advance.class));
//
//        advanceService = mock(AdvanceService.class);
//        when(advanceService.getAllAdvances()).thenReturn(randomListOf(88, Advance.class));
//        when(advanceService.cancelAdvancePayment(any(),any())).thenReturn(random(ResponseEntity.class));
//        when(advanceService.confirmAdvance(any())).thenReturn(random(ResponseEntity.class));
//        when(advanceService.changeAdvanceComment(any())).thenReturn(random(ResponseEntity.class));
//        when(advanceService.setLoadingComplete(any(),any())).thenReturn(random(ResponseEntity.class));
//        when(advanceService.findTrip(any())).thenReturn(random(Trip.class));
//        when(advanceService.findAdvancesWithoutFiles()).thenReturn(randomListOf(44, Advance.class));
//        when(advanceService.findById(any())).thenReturn(random(Advance.class));
//        when(advanceService.findByTripNum(any())).thenReturn(random(Advance.class));
//        when(advanceService.findByTripId(any())).thenReturn(random(Advance.class));
//        when(advanceService.saveAll(any())).thenReturn(randomListOf(25, Advance.class));
//        when(advanceService.saveAll(any())).thenReturn(randomListOf(25, Advance.class));
//
//
//        mapper = AdvanceMapper.INSTANCE;
//
//    }
//
//
//    @Test
//    void testAdvanceService() {
//
//            Person person = personService.getPerson(666l);
//            Trip trip = tripService.findTripById(77l);
//            Advance advance = advanceService.createAdvanceForTripAndAuthorId(person.getId(), trip.getId());
//
//            advanceService.setEmailRead(advance);
//            advanceService.setLoadingComplete(advance.getId(), true);
//            advanceService.cancelAdvancePayment(advance.getAdvanceTripFields().getTripId(), "cancell");
//            advanceService.confirmAdvance(advance.getId());
//            advanceService.saveAdvance(advance);
//
//            assertNotNull(advance);
//            assertNotNull(trip);
//            assertNotNull(person);
//            assertNotNull(advance.getCostInfo());
//            assertNotNull(advance.getContact());
//
//
//    }
}
