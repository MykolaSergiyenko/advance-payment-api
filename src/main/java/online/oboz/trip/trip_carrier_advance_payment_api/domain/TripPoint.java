package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import java.time.OffsetDateTime;

public class TripPoint {

    private String additionalServiceCode;
    private String additionalServiceName;
    private Integer position;
    private String address;
    private OffsetDateTime pickDate;
    private OffsetDateTime pickEndDate;
    private OffsetDateTime factStartDate;
    private OffsetDateTime factEndDate;
    private OffsetDateTime arrivalDate;
    private OffsetDateTime arrivalDateCorrected;
    private String status;

}
