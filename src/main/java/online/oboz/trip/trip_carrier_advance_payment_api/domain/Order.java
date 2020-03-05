package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import java.util.List;

@Data
public class Order {

    private String orderNum;
    private String orderStatus;
    private String originOrderNum;
    private String orderType;
    private List<OrderPoint> orderPoints;
    private Contractor client;
    private String contractName;
    private String contractNum;
    private String contractPaymentContractorName;
    private Contractor paymentContractor;
    private Person author;
    private Double cost;
    private String currencyCode;
    private Integer currencyCourse;
    private Vat vat;
    private String comment;
    private List<Trip> trips;

}
