package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.contracts.HasContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;

import javax.persistence.*;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AdvanceContact extends HasContractor {

    @AttributeOverrides({
        @AttributeOverride(name="fullName", column=@Column(name="fullName")),
        @AttributeOverride(name="phone", column=@Column(name="phone")),
        @AttributeOverride(name="email", column=@Column(name="email"))
    })
    @Embedded
    private FullNamePersonInfo info;

    public AdvanceContact() {
    }

    public FullNamePersonInfo getInfo() {
        return info;
    }

    public void setInfo(FullNamePersonInfo info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "AdvanceContact{" +
            "info=" + info +
            '}';
    }
}
