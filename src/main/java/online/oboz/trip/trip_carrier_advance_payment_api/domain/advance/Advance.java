package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance.ContactableAdvance;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;


/**
 * "Заявка на авансирование" или просто "Аванс по трипу"
 * <p>
 * Основная сущность сервиса "Авансирование"
 * и так называемого "Автоавансирования" -
 * мы ее создаем, изменяем и так далее
 * поэтому к ней быть особенно внимательным
 * <p>
 * Таблица находится в схеме "Заказов" - orders
 * <p>
 */

@Entity
@Table(name = "trip_request_advance_payment", schema = "orders")
@AttributeOverride(name = "uuid", column = @Column(name = "advance_uuid"))
public class Advance extends ContactableAdvance {
    final static Logger log = LoggerFactory.getLogger(Advance.class);


    @NaturalId
    @NotNull
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "advance_uuid", length = 36, updatable = false, insertable = false)
    private UUID advanceUuid;


    //TODO: @Type(type = "uuid-char")@Column(length = 36)
    @Column(name = "uuid_contract_application_file")
    private String uuidContractApplicationFile;

    //TODO: @Type(type = "uuid-char")@Column(length = 36)
    @Column(name = "uuid_advance_application_file")
    private String uuidAdvanceApplicationFile;


    @Column(name = "loading_complete", columnDefinition = "boolean default false")
    private Boolean loadingComplete;


    public Advance() {

    }

    public Advance(Person author) {
        setAuthor(author);
        setAuthorId(author.getId());
    }


    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
        setAdvanceUuid(UUID.randomUUID());
        setLoadingComplete(false);
    }


    public Boolean getLoadingComplete() {
        return loadingComplete;
    }


    public String getUuidContractApplicationFile() {
        return uuidContractApplicationFile;
    }

    public String getUuidAdvanceApplicationFile() {
        return uuidAdvanceApplicationFile;
    }


    public void setLoadingComplete(Boolean loadingComplete) {
        this.loadingComplete = loadingComplete;
    }


    public void setUuidContractApplicationFile(String uuidContractApplicationFile) {
        this.uuidContractApplicationFile = uuidContractApplicationFile;
    }

    public void setUuidAdvanceApplicationFile(String uuidAdvanceApplicationFile) {
        this.uuidAdvanceApplicationFile = uuidAdvanceApplicationFile;
    }


    public UUID getAdvanceUuid() {
        return advanceUuid;
    }

    public void setAdvanceUuid(UUID advanceUuid) {
        this.advanceUuid = advanceUuid;
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
        return "TripAdvance{" +
            ", uuidContractApplicationFile='" + uuidContractApplicationFile + '\'' +
            ", uuidAdvanceApplicationFile='" + uuidAdvanceApplicationFile + '\'' +
            ", loadingComplete=" + loadingComplete +
            '}';
    }
}
