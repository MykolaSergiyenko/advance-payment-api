package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(schema = "common", name = "persons")
public class Person {

    @Id
    private Long id;
    private Long contractorId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String email;

}
