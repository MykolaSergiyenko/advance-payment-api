package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class TripDocuments {
    @JsonProperty("trip_documents")
    public List<TripDocument> tripDocuments;

    public TripDocuments() {
    }

    public List<TripDocument> getTripDocuments() {
        return this.tripDocuments;
    }

    public void setTripDocuments(List<TripDocument> tripDocuments) {
        this.tripDocuments = tripDocuments;
    }

    public String toString() {
        return "TripDocuments(tripDocuments=" + this.getTripDocuments() + ")";
    }
}
