package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "orders", name = "orders")
public class Order {
    @Id
    private Long id;
    private Long orderTypeId;

    public Order() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getOrderTypeId() {
        return this.orderTypeId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderTypeId(Long orderTypeId) {
        this.orderTypeId = orderTypeId;
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
        return "Order{" +
            "id=" + id +
            ", orderTypeId=" + orderTypeId +
            '}';
    }
}
