package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.structures;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Formula;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

@Embeddable
public class TripCostInfo implements Serializable {

//    @Column(name="contractor_id")
//    private Long contractorId;


    @Column(name = "cost")
    private Double cost;


    @Column(name = "vat_code", columnDefinition = "default 'twenty_five'")
    private String vatCode;


    @Formula(value = ("case when (contractor_id > 0 and vat_code is not null) then " +
        "(cost + " +
        " (cost * " +
        "   (case when (select ccontr.is_vat_payer from " +
        "              common.contractors ccontr where ccontr.id = contractor_id) " +
        "    then 1 else 0 end) * "+
        " (select (dv.value/100) from dictionary.vats dv where dv.code = vat_code)" +
        "))::numeric(19, 2) " +
        "else cost::numeric(19, 2) end "))
    @Column(name = "nds_cost")
    private Double ndsCost;

    public TripCostInfo() {
    }



    public void setNdsCost(Double ndsCost) {
        this.ndsCost = ndsCost;
    }


    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }


    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

//    public Long getContractorId() {
//        return contractorId;
//    }

    @Transient
    public Double getNdsCost() {
        return ndsCost;
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
//            "contractorId=" + contractorId +
            ", cost=" + cost +
            ", vatCode='" + vatCode + '\'' +
            ", ndsCost=" + ndsCost +
            '}';
    }
}
