package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
public class AdvanceCostDict {
    final static Logger log = LoggerFactory.getLogger(AdvanceCostDict.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "advance_payment_sum")
    private Double advancePaymentSum;

    @Column(name = "registration_fee")
    private Double registrationFee;

//    @Column(name = "created_at", insertable = false, updatable = false)
//    private OffsetDateTime createdAt;

    public AdvanceCostDict() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getAdvancePaymentSum() {
        return advancePaymentSum;
    }

    public void setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
    }

    public Double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
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
        return "Advance Cost Dictionary {" +
            "id=" + id +
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", advancePaymentSum=" + advancePaymentSum +
            ", registrationFee=" + registrationFee +
//            ", createdAt=" + createdAt +
            '}';
    }
}
