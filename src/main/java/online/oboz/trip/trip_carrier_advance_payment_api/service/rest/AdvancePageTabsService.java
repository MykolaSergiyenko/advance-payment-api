package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Paginator;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdvancePageTabsService {
    private final ApplicationProperties applicationProperties;
    private final AdvanceService advanceService;


    private final String autoComment;

    public AdvancePageTabsService(
        ApplicationProperties applicationProperties,
        AdvanceService advanceService) {
        this.applicationProperties = applicationProperties;
        autoComment = applicationProperties.getAutoCreatedComment();
        this.advanceService = advanceService;
    }

    public ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter) {
        return advanceService.searchInWorkRequests(filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter) {
        return advanceService.searchProblemRequests(filter);

    }

    public ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter) {
        return advanceService.searchPaidRequests(filter);

    }

    public ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter) {
        return advanceService.searchNotPaidRequests(filter);

    }

    public ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter) {
        return advanceService.searchCanceledRequests(filter);
    }

}
