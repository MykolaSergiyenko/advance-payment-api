package online.oboz.trip.trip_carrier_advance_payment_api.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


// Don't use it.

// How to use in BStore-Srrvice???


public class BstoreResponse {

    @JsonProperty("file_uuid")
    private String fileUuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("link")
    private String link;

    public BstoreResponse() {
    }

    public String getFileUuid() {
        return this.fileUuid;
    }

    public String getName() {
        return this.name;
    }

    public String getLink() {
        return this.link;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLink(String link) {
        this.link = link;
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
        //log it?
        return "BstoreResponse{" +
            "fileUuid='" + fileUuid + '\'' +
            ", name='" + name + '\'' +
            ", link='" + link + '\'' +
            '}';
    }
}
