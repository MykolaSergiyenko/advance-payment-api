package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



    public Map<String, String> findAdvanceRequestDocsFileMap() {
        return getFileMap("assignment_advance_request");
    }

    public Map<String, String> findTripRequestDocsFileMap() {
        return getFileMap("trip_request", "request");
    }


    public Map<String, String> getFileMap(String... types) {
        List<String> expectedTypes = Arrays.asList(types);
        Map<String, String> fileUuidMap = new HashMap<>();
        tripDocuments.stream().filter(doc -> doc.getFileId() != null).
            filter(doc -> expectedTypes.contains(doc.getDocumentTypeCode())).
            forEach(doc -> fileUuidMap.put(doc.getDocumentTypeCode(), doc.getFileId()));
        return fileUuidMap;
    }


    @Override
    public String toString() {
        return "TripDocuments{" +
            "tripDocuments=" + tripDocuments +
            '}';
    }
}
