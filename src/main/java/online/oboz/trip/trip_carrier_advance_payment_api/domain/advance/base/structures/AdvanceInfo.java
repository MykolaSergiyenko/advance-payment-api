package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;


import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class AdvanceInfo {

    @Column(name = "advance_payment_sum")
    private Double advancePaymentSum;


    @Column(name = "registration_fee")
    private Double registrationFee;

    public AdvanceInfo() {
    }

    public AdvanceInfo(Double advancePaymentSum, Double registrationFee) {
        this.advancePaymentSum = advancePaymentSum;
        this.registrationFee = registrationFee;
    }

    public Double getAdvancePaymentSum() {
        return advancePaymentSum;
    }

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
