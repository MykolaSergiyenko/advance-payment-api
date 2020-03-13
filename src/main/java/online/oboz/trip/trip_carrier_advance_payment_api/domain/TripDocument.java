package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class TripDocument {
    @JsonProperty
    public Integer id;
    @JsonProperty("trip_id")
    public Integer tripId;
    @JsonProperty("file_id")
    public String fileId;
    @JsonProperty("template_file_id")
    public UUID templateFileId;
    @JsonProperty("document_type_code")
    public String documentTypeCode;
    @JsonProperty("document_properties_values")
    public Object documentPropertiesValues;
    @JsonProperty("name")
    public String name;
}
