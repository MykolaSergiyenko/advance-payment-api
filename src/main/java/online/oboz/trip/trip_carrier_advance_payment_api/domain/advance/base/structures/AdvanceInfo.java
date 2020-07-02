package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;


import org.hibernate.annotations.Formula;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class AdvanceInfo {

    @Column(name = "advance_payment_sum")
    @Formula(value = ("case when (nds_cost > 0) then " +
        "   (select dcost.advance_payment_sum::numeric(19, 2) " +
        "       from dictionary.advance_payment_cost  dcost " +
        "           where nds_cost between dcost.min_value and dcost.max_value) " +
        " else '0.00'::text::numeric(19, 2) end "))
    private Double advancePaymentSum;


    @Column(name = "registration_fee")
    @Formula(value = ("case when (nds_cost > 0) then " +
        "   (select dcost.registration_fee::numeric(19, 2) " +
        "       from dictionary.advance_payment_cost  dcost " +
        "           where nds_cost between dcost.min_value and dcost.max_value) " +
        "   else '0.00'::text::numeric(19, 2) end"))
    private Double registrationFee;

    public AdvanceInfo() {
    }

    @Transient
    public Double getAdvancePaymentSum() {
        return advancePaymentSum;
    }

    @Transient
    public Double getRegistrationFee() {
        return registrationFee;
    }

    public void setAdvancePaymentSum(Double advancePaymentSum) {
        this.advancePaymentSum = advancePaymentSum;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }

    @Override
    public String toString() {
        return "TripAdvanceInfo{" +
            "advancePaymentSum=" + advancePaymentSum +
            ", registrationFee=" + registrationFee +
            '}';
    }
}
