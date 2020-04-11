package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TripDocuments {
    @JsonProperty("trip_documents")
    private List<TripDocument> tripDocuments;

    public TripDocuments() {
    }

    public List<TripDocument> getTripDocuments() {
        return this.tripDocuments;
    }

    public void setTripDocuments(List<TripDocument> tripDocuments) {
        this.tripDocuments = tripDocuments;
    }

    @Override
    public String toString() {
        return "TripDocuments{" +
            "tripDocuments=" + tripDocuments +
            '}';
    }
}
