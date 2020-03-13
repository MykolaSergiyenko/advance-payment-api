package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties
public class TripDocuments {
    @JsonProperty("trip_documents")
    public List<TripDocument> tripDocuments;
}
