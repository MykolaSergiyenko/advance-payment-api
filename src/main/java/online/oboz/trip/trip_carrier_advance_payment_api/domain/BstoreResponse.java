package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BstoreResponse {

    @JsonProperty("file_uuid")
    String fileUuid;
    @JsonProperty("name")
    String name;
    @JsonProperty("link")
    String link;

}
