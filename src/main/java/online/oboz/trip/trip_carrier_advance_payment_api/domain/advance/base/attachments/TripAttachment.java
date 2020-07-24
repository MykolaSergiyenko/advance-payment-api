package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Вложения, загружаемые в Трип
 */
@Entity
@Table(name = "trip_documents", schema = "orders")
public class TripAttachment {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * UUID файла
     */
    @Column(name = "file_id", nullable = false)
    private UUID fileId;



    /**
     * Trip's id
     */
    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    /**
     * Attachment's type code
     */
    @Column(name = "document_type_code", nullable = false)
    private String documentTypeCode;


    /**
     * Attachment's type name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * ?
     */
    @Column(name = "template_file_id")
    private UUID templateFileId;


    public TripAttachment() {
    }

    public TripAttachment (Long tripId, UUID fileUuid){
        setTripId(tripId);
        setFileId(fileUuid);
        setDocumentTypeCode("assignment_advance_request");
        setName("Заявка на авансирование");
    }


    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getTemplateFileId() {
        return templateFileId;
    }

    public void setTemplateFileId(UUID templateFileId) {
        this.templateFileId = templateFileId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "TripAttachment{" +
            "id=" + id +
            ", fileId=" + fileId +
            ", tripId=" + tripId +
            ", documentTypeCode='" + documentTypeCode + '\'' +
            ", name='" + name + '\'' +
            ", templateFileId=" + templateFileId +
            '}';
    }
}
