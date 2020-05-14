package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TripAdvanceEntityTests {

    @Autowired
    private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager entityManager;
    @Autowired private TripAdvanceRepository tripAdvanceRepository;


//    @Test
//    void injectedComponentsAreNotNull(){
//        assertNotNull(dataSource);
//        assertNotNull(jdbcTemplate);
//        assertNotNull(entityManager);
//        assertNotNull(tripAdvanceRepository);
//    }
//    @Test
//    void testTripAdvanceRepository(){
//
//        TripAdvance t = tripAdvanceRepository.find(1488l).orElseGet(null);
//        System.out.println("--- t.id = "+t.getId());
//    }

}
