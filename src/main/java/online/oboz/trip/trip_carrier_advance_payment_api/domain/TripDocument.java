package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import java.util.UUID;

public class TripDocument {

    public Integer id;
    public Integer tripId;
    public String fileId;
    public UUID templateFileId;
    public String documentTypeCode;
    public Object documentPropertiesValues;
    public String name;

}
