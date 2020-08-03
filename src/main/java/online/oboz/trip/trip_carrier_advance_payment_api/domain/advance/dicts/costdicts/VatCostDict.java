package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.costdicts;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Справочник НДС
 *
 * @author Ⓐbo3
 */
@Entity
@Table(name = "vats", schema = "dictionary")
public class VatCostDict {
    @Id
    @Column(name = "code")
    private String code;


    @Column(name = "name")
    private String name;


    @Column(name = "value")
    private Integer value;

    public VatCostDict() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
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
        return "VatCostDict{" +
            "name=" + name +
            ", code=" + code +
            ", value=" + value +
            '}';
    }
}
