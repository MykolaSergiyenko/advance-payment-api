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
    private String orderNum;
    private String orderStatus;
    private String originOrderNum;
    private Long orderTypeId;
    private String contractPaymentContractorName;

    public Order() {
    }

    public Long getId() {
        return this.id;
    }

    public String getOrderNum() {
        return this.orderNum;
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }

    public String getOriginOrderNum() {
        return this.originOrderNum;
    }

    public Long getOrderTypeId() {
        return this.orderTypeId;
    }

    public String getContractPaymentContractorName() {
        return this.contractPaymentContractorName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOriginOrderNum(String originOrderNum) {
        this.originOrderNum = originOrderNum;
    }

    public void setOrderTypeId(Long orderTypeId) {
        this.orderTypeId = orderTypeId;
    }

    public void setContractPaymentContractorName(String contractPaymentContractorName) {
        this.contractPaymentContractorName = contractPaymentContractorName;
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
            ", orderNum='" + orderNum + '\'' +
            ", orderStatus='" + orderStatus + '\'' +
            ", originOrderNum='" + originOrderNum + '\'' +
            ", orderTypeId=" + orderTypeId +
            ", contractPaymentContractorName='" + contractPaymentContractorName + '\'' +
            '}';
    }
}
