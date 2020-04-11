package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "common", name = "contractors")
public class Contractor {

    @Id
    private Long id;
    private String fullName;
    private Boolean isVatPayer;
    private Boolean isVerified;
    private String phone;
    private String email;
    private Boolean isAutoAdvancePayment;

    public Contractor() {
    }

    public Long getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Boolean getIsVatPayer() {
        return this.isVatPayer;
    }

    public Boolean getIsVerified() {
        return this.isVerified;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getEmail() {
        return this.email;
    }

    public Boolean getIsAutoAdvancePayment() {
        return this.isAutoAdvancePayment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIsVatPayer(Boolean isVatPayer) {
        this.isVatPayer = isVatPayer;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsAutoAdvancePayment(Boolean isAutoAdvancePayment) {
        this.isAutoAdvancePayment = isAutoAdvancePayment;
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
        return "Contractor{" +
            "id=" + id +
            ", fullName='" + fullName + '\'' +
            ", isVatPayer=" + isVatPayer +
            ", isVerified=" + isVerified +
            ", phone='" + phone + '\'' +
            ", email='" + email + '\'' +
            ", isAutoAdvancePayment=" + isAutoAdvancePayment +
            '}';
    }
}
