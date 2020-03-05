package online.oboz.trip.trip_carrier_advance_payment_api.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueryResponse {

    private List<FrontOrder> orderList = new ArrayList<>();

}
