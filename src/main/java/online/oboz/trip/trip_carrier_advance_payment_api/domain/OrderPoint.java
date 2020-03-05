package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class OrderPoint {

    private String address;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String serviceTypeCode;
    private int position;

}
