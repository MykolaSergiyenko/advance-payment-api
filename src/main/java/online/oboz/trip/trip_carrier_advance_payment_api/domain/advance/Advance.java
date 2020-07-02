package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.advance.ContactableAdvance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContact;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.hibernate.annotations.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.PersistenceConstructor;


import javax.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

//@AttributeOverride(name = "trip_comment", column = @Column(name = "trip_comment"))

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
 * //TODO: Переименовать таблицу в БД в trip_advances
 */

@Entity
@Table(name = "trip_request_advance_payment", schema = "orders")
public class Advance extends ContactableAdvance {
    final static Logger log = LoggerFactory.getLogger(Advance.class);
//
//
//    @Column(name = "push_button_at")
//    private OffsetDateTime pushButtonAt;

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
//
//    @PersistenceConstructor
//    public Advance(Trip trip, Person autoAuthor, AdvanceContactsBook contact) {
//        this.setUuid(UUID.randomUUID());
//        this.setAdvanceTripFields(trip.getTripFields());
//        this.setCostInfo(trip.getTripCostInfo());
//        this.setAuthor(autoAuthor);
//        this.setContact(contact);
//        //this.setTripAdvanceInfo();
//    }



    @PrePersist
    @Override
    public void onCreate() {
        super.onCreate();
        setLoadingComplete(false);
        onUpdate();
    }




    @PreUpdate
    public void onUpdate() {
        log.info("**** On-update advance:"+this.toString());
//
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
