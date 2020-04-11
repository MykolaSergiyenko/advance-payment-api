package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "orders", name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractorId;
    private Long driverId;
    private String num;
    private Long orderId;
    private Double cost;
    private Long paymentContractorId;
    private String vatCode;
    private String tripTypeCode;
    private String tripStatusCode;
    private String resourceTypeCode;
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TripInfo tripInfo;

    public Trip() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getContractorId() {
        return this.contractorId;
    }

    public Long getDriverId() {
        return this.driverId;
    }

    public String getNum() {
        return this.num;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Double getCost() {
        return this.cost;
    }

    public Long getPaymentContractorId() {
        return this.paymentContractorId;
    }

    public String getVatCode() {
        return this.vatCode;
    }

    public String getTripTypeCode() {
        return this.tripTypeCode;
    }

    public String getTripStatusCode() {
        return this.tripStatusCode;
    }

    public String getResourceTypeCode() {
        return this.resourceTypeCode;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public void setPaymentContractorId(Long paymentContractorId) {
        this.paymentContractorId = paymentContractorId;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public void setTripTypeCode(String tripTypeCode) {
        this.tripTypeCode = tripTypeCode;
    }

    public void setTripStatusCode(String tripStatusCode) {
        this.tripStatusCode = tripStatusCode;
    }

    public void setResourceTypeCode(String resourceTypeCode) {
        this.resourceTypeCode = resourceTypeCode;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public TripInfo getTripInfo() {
        return tripInfo;
    }

    public void setTripInfo(TripInfo tripInfo) {
        this.tripInfo = tripInfo;
    }

    @Override
    public String toString() {
        return "Trip{" +
            "id=" + id +
            ", contractorId=" + contractorId +
            ", driverId=" + driverId +
            ", num='" + num + '\'' +
            ", orderId=" + orderId +
            ", cost=" + cost +
            ", paymentContractorId=" + paymentContractorId +
            ", vatCode='" + vatCode + '\'' +
            ", tripTypeCode='" + tripTypeCode + '\'' +
            ", tripStatusCode='" + tripStatusCode + '\'' +
            ", resourceTypeCode='" + resourceTypeCode + '\'' +
            ", createdAt=" + createdAt +
            ", tripInfo=" + tripInfo +
            '}';
    }
}
