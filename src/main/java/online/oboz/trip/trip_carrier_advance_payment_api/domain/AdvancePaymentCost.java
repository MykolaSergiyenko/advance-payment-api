package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
public class AdvancePaymentCost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Double minValue;
    private Double maxValue;
    private Double advancePaymentSum;
    private Double registrationFee;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public AdvancePaymentCost() {
    }

    public Long getId() {
        return this.id;
    }

    public Double getMinValue() {
        return this.minValue;
    }

    public Double getMaxValue() {
        return this.maxValue;
    }

    public Double getAdvancePaymentSum() {
        return this.advancePaymentSum;
    }

    public Double getRegistrationFee() {
        return this.registrationFee;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public void setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
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
        return "AdvancePaymentCost{" +
            "id=" + id +
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", advancePaymentSum=" + advancePaymentSum +
            ", registrationFee=" + registrationFee +
            ", createdAt=" + createdAt +
            '}';
    }
}
