package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts;


import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseEntity;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures.AdvanceInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


/**
 * Справоник сумм и сборов аванса
 *
 * @author Ⓐbo3
 */
@Entity
@Table(name = "advance_payment_cost", schema = "dictionary")
public class AdvanceCostDict extends BaseEntity {

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    /**
     * Advance costs calculated by Trip params and dictionaries
     */
    @AttributeOverrides({
        @AttributeOverride(name = "advancePaymentSum", column = @Column(name = "advance_payment_sum")),
        @AttributeOverride(name = "registrationFee", column = @Column(name = "registration_fee"))
    })
    @Embedded
    private AdvanceInfo tripAdvanceInfo;

    public AdvanceCostDict() {
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
        return tripAdvanceInfo.getAdvancePaymentSum();
    }

    public void setAdvancePaymentSum(Double advancePaymentSum) {
        this.tripAdvanceInfo.setAdvancePaymentSum(advancePaymentSum);;
    }

    public Double getRegistrationFee() {
        return tripAdvanceInfo.getAdvancePaymentSum();
    }

    public void setRegistrationFee(Double registrationFee) {
        this.tripAdvanceInfo.setRegistrationFee(registrationFee);
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
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", advancePaymentSum=" + tripAdvanceInfo.getAdvancePaymentSum() +
            ", registrationFee=" + tripAdvanceInfo.getRegistrationFee() +
            '}';
    }
}
