package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "orders", name = "trips")
public class Trip {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, insertable = false)
    private Long id;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "trip_type_code")
    private String tripTypeCode;


    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "contractor_id", referencedColumnName = "id")
    })
    private Contractor contractor;



    //map on advance?
    private String num;

    //map on advance?
    @Column(name = "order_id", updatable = false, insertable = false)
    private Long orderId;

    //map on advance?
    private Double cost;

    @NotNull
    @Column(name = "payment_contractor_id")
    private Long paymentContractorId;

    @NotNull
    @Column(name = "contractor_id", updatable = false, insertable = false)
    private Long contractorId;

    //map on dict?
    private String vatCode;

    //map on advance status?
    private String tripStatusCode;

    //what is it?
    private String resourceTypeCode;
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private TripInfo tripInfo;

//
//    @JoinColumn(name = "driver_id", referencedColumnName = "driver_id"),
//    @JoinColumn(name = "contractor_id", referencedColumnName = "contractor_id"),
//    @JoinColumn(name = "payment_contractor_id", referencedColumnName = "payment_contractor_id"),
//    @JoinColumn(name = "trip_type_code", referencedColumnName = "trip_type_code", columnDefinition = "motor")
    //or many?

    @OneToOne(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "trip_id")
    private TripAdvance trip_advance;



    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "id")
    })
    private Order order;

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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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
            '}';
    }
}
