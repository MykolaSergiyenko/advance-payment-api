package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import java.util.List;

@Data
public class Trip {

    private int tripId;
    private String tripNum;
    private String tripStatus;
    private String tripStatusCode;
    private String tripType;
    private String tripTypeCode;
    private Contractor contractor;
    private Person driver;
    private String resourceInfo;
    //    private Couple couple;
    private String contractName;
    private String contractNum;
    private Double cost;
    private String currencyCode;
    private Integer currencyCourse;
    private Vat vat;
    private String comment;
    private String truckBodyTypeCode;
    private List<TripPoint> tripPoints;

}
