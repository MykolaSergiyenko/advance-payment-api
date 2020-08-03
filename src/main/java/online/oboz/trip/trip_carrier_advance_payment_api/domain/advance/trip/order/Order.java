package online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.order;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.entities.BaseUuidEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * Заказ-ордер - в общем случае может иметь несколько Трипов
 *
 * @author Ⓐbo3
 */
@Entity
@Table(schema = "orders", name = "orders")
public class Order extends BaseUuidEntity {


    // only .MOTOR orders
    /**
     * Order's type
     */
    @Column(name = "order_type_id")
    private Long orderTypeId = 1l;

    public Order() {
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
            super.toString() +
            '}';
    }
}
