package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ReportService {
    ResponseEntity<Resource> downloadAdvanceTemplate(Advance advance);
}
