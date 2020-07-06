package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface BaseAdvanceService {

    Trip findTrip(Long tripId);

    void giveAutoAdvances(Person autoUser);

    Advance createAdvanceForTripAndAuthorId(Long tripId, Long authorId);

    List<Advance> getAllAdvances();

    Advance findById(Long id);

    Advance findByUuid(UUID uuid);

    Advance findByTripNum(String tripNum);

    Advance findByTripId(Long tripId);

    List<Advance> findAdvancesWithoutFiles();

    ResponseEntity<Void> confirmAdvance(Long advanceId);

    ResponseEntity<Void> cancelAdvancePayment(Long tripId, String withComment);

    ResponseEntity<Void> changeAdvanceComment(AdvanceCommentDTO commentDTO);

    ResponseEntity<Void> setLoadingComplete(Long tripId, Boolean loadingComplete);

    Advance saveAdvance(Advance advance);

    List<Advance> saveAll(List<Advance> advances);

    void notifyAboutAdvance(Advance advance);

    void notifyAboutAdvanceScheduled(Advance advance);

    void notifyUnread();

    void setEmailRead(Advance advance);

}
