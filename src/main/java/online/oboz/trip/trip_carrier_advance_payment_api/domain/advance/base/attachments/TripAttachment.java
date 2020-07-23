package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUpdateEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Вложения, загружаемые в Трип
 */
@Entity
@Table(name = "trip_documents", schema = "orders")
public class TripAttachment extends BaseUpdateEntity {

    /**
     * UUID файла
     */
    @NaturalId
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
        this.setTripId(tripId);
        this.setFileId(fileUuid);
        this.setDocumentTypeCode("assignment_advance_request");
        this.setName("Заявка на авансирование");
    }

    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
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
            "fileId=" + fileId +
            ", templateFileId=" + templateFileId +
            ", tripId=" + tripId +
            ", documentTypeCode='" + documentTypeCode + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
