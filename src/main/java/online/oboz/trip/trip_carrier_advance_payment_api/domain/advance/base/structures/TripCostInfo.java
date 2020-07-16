package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Цены в трипе
 */
@Embeddable
public class TripCostInfo implements Serializable {


    /**
     * Trip's cost without VAT
     */
    @Column(name = "cost")
    private Double cost;

    public TripCostInfo() {
    }

    public TripCostInfo(Double cost) {
        this.cost = cost;
    }


    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
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
        return "TripCostInfo{" +
            ", cost=" + cost +
            '}';
    }
}
