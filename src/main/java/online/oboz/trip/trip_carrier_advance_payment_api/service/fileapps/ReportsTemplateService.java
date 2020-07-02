package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.OK;

@Service
public class ReportsTemplateService {

    private static final Logger log = LoggerFactory.getLogger(ReportsTemplateService.class);

    private final RestService restService;
    private final ApplicationProperties applicationProperties;

    public ReportsTemplateService(AdvanceRepository advanceRepository,
                                  RestService restService,
                                  ApplicationProperties applicationProperties) {
        this.restService = restService;
        this.applicationProperties = applicationProperties;
    }


    public ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance) {
        if (advance == null) {
            throw getReportsError("Reports-service template's advance is null.");
        } else {
            String reportsUrl = getUrlForReportService(advance);
            log.info("Report-server URL for advance {} is: {} ", advance.getId(), reportsUrl);
            ResponseEntity<Resource> response = restService.getRequestResource(reportsUrl, new HttpHeaders());
            if (response.getStatusCode() == OK) {
                log.info("Reports server response Headers is: {}", response.getHeaders().entrySet().toString());
                return response;
            } else {
                log.error("Report-Server returned bad-response for Advance id: {}. Response is: {}", advance, response);
                return null;
            }
        }
    }

    private String getUrlForReportService(Advance advance) {
        String params = formatParams(advance);
        return String.join("",
            applicationProperties.getReportsUrl().toString(),
            applicationProperties.getReportsTypeKey(), applicationProperties.getReportsUser(),
            applicationProperties.getReportsApiKey(), params, applicationProperties.getReportsFormat());
    }

    private String formatParams(Advance advance){
        return String.format(applicationProperties.getReportsParams(),
            advance.getAdvanceTripFields().getNum(),
            advance,
            advance.getTripAdvanceInfo().getRegistrationFee());
    }

    private BusinessLogicException getReportsError(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
}
