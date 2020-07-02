package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUpdateEntity;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


//@Entity
//@Table(schema = "common", name = "locations")
public class CommonLocation extends BaseUpdateEntity {

    @NaturalId
    @NotNull
    @Column(name = "location_id")
    private String locationId;

    @Column(name = "location_tz")
    private String locationTz;

    // Address as abstract?
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
