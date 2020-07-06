package online.oboz.trip.trip_carrier_advance_payment_api.domain.response.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TripDocument {
    @JsonProperty
    private Integer id;
    @JsonProperty("trip_id")
    private Integer tripId;
    @JsonProperty("file_id")
    private String fileId;
    @JsonProperty("template_file_id")
    private UUID templateFileId;
    @JsonProperty("document_type_code")
    private String documentTypeCode;
    @JsonProperty("document_properties_values")
    private Object documentPropertiesValues;
    @JsonProperty("name")
    private String name;

    public TripDocument() {
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getTripId() {
        return this.tripId;
    }

    public String getFileId() {
        return this.fileId;
    }

    public UUID getTemplateFileId() {
        return this.templateFileId;
    }

    public String getDocumentTypeCode() {
        return this.documentTypeCode;
    }

    public Object getDocumentPropertiesValues() {
        return this.documentPropertiesValues;
    }

    public String getName() {
        return this.name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setTemplateFileId(UUID templateFileId) {
        this.templateFileId = templateFileId;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public void setDocumentPropertiesValues(Object documentPropertiesValues) {
        this.documentPropertiesValues = documentPropertiesValues;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "TripDocument{" +
            "id=" + id +
            ", tripId=" + tripId +
            ", fileId='" + fileId + '\'' +
            ", templateFileId=" + templateFileId +
            ", documentTypeCode='" + documentTypeCode + '\'' +
            ", documentPropertiesValues=" + documentPropertiesValues +
            ", name='" + name + '\'' +
            '}';
    }
}
