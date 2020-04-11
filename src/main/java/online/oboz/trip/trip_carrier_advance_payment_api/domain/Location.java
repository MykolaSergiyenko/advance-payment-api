package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "common", name = "locations")
public class Location {
    @Id
    private Long id;
    private String locationId;
    private String locationTz;
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
            "id=" + id +
            ", locationId='" + locationId + '\'' +
            ", locationTz='" + locationTz + '\'' +
            ", address='" + address + '\'' +
            '}';
    }
}
