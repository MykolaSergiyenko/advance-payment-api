package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUpdateEntity;
import org.hibernate.annotations.NaturalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


/**
 * Точка маршрута
 */
@Entity
@Table(schema = "common", name = "locations")
public class CommonLocation extends BaseUpdateEntity {
    final static Logger log = LoggerFactory.getLogger(CommonLocation.class);


    /**
     * Location id
     */
    @NaturalId
    @NotNull
    @Column(name = "location_id")
    private String locationId;

    /**
     * Location time-zone
     */
    @Column(name = "location_tz")
    private String locationTz;

    /**
     * Location address
     */
    @Column(name = "address")
    private String address;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationTz() {
        return locationTz;
    }

    public void setLocationTz(String locationTz) {
        this.locationTz = locationTz;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Location{" +
            super.toString() +
            ", locationId='" + locationId +
            ", locationTz='" + locationTz +
            ", address='" + address +
            '}';
    }
}
