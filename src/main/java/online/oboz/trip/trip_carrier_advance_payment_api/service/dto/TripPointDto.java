package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TripPointDto {

    @JsonProperty
    private Integer tripId;
    @JsonProperty
    private String address;
    @JsonProperty
    private String additionalServiceCode;
    @JsonProperty
    private String num;

}
