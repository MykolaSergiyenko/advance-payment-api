package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.PageService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdvancePageTabsService implements AdvanceTabManager {
    private static final Logger log = LoggerFactory.getLogger(AdvancePageTabsService.class);

    private final PageService pageService;

    public AdvancePageTabsService(PageService pageService) {
        this.pageService = pageService;
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter) {
        return pageService.searchInWorkRequests(filter);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter) {
        return pageService.searchProblemRequests(filter);

    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter) {
        return pageService.searchPaidRequests(filter);

    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter) {
        return pageService.searchNotPaidRequests(filter);

    }


    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter) {
        return pageService.searchCanceledRequests(filter);
    }
}
