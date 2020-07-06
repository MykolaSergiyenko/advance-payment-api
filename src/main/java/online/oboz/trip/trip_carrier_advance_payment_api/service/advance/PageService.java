package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PageService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceService.class);


    private final AdvanceMapper mapper = AdvanceMapper.INSTANCE;
    private final AdvanceService advanceService;
    private final ApplicationProperties applicationProperties;


    public PageService(AdvanceService advanceService, ApplicationProperties applicationProperties) {
        this.advanceService = advanceService;
        this.applicationProperties = applicationProperties;
    }

    public Advance setComment(AdvanceCommentDTO comment){
        return mapper.setComment(comment);
    }


    private ResponseEntity<AdvanceDesktopDTO> mapToDesktop(List<Advance> advances, Filter filter) {
        return new ResponseEntity<>(mapToAdvanceDesktop(advances, filter),
            HttpStatus.OK);
    }


    public ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter) {
        List<Advance> all = advanceService.getAllAdvances().stream()
            .filter(advance -> !advance.isProblem(applicationProperties.getAutoCreatedComment())
                && !advance.isPaid() && !advance.isCancelled()).collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter) {
        String comment = applicationProperties.getAutoCreatedComment();
        log.info("comment:"+comment);

        List<Advance> all = advanceService.getAllAdvances();
        log.info("all.count:"+advanceService.getAllAdvances().size());

        all = all.stream().
            filter(advance -> advance.isProblem(comment)).
            collect(Collectors.toList());
        log.info("all.count 2:"+all.size());

        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter) {
        List<Advance> all = advanceService.getAllAdvances().stream().
            filter(advance -> advance.isPaid()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter) {
        List<Advance> all = advanceService.getAllAdvances().stream().
            filter(advance -> !advance.isPaid()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }

    public ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter) {
        List<Advance> all = advanceService.getAllAdvances().stream().
            filter(advance -> advance.isCancelled()).
            collect(Collectors.toList());
        return mapToDesktop(all, filter);
    }



    private AdvanceDesktopDTO mapToAdvanceDesktop(List<Advance> advances, Filter filter) {
        List<Advance> responseList = advances.stream()
            .skip(getOffset(filter.getPage(), filter.getPer()))
            .limit(filter.getPer())
            .collect(Collectors.toList());

        //List<AdvanceDTO> dtoList = mapper.toAdvancesDTO(responseList);
        AdvanceDesktopDTO desktop = new AdvanceDesktopDTO();
        List<AdvanceDTO> list = mapper.toAdvancesDTO(responseList);
        desktop.setAdvances(list);
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
}