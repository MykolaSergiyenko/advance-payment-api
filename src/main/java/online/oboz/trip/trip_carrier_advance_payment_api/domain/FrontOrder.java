package online.oboz.trip.trip_carrier_advance_payment_api.domain;


import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FrontOrder {

    private int tripId;
    private int orderId;
    private String tripTypeCode;
    private String orderNum;
    private String tripNum;
    private String originOrderNum;
    private Contractor client;
    private Contractor carrier;
    private OffsetDateTime factStartDate;
    private String tripStatusCode;
    private Person driver;
    private Person dispatcher;

}
