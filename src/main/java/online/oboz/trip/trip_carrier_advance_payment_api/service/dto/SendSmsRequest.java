package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendSmsRequest {
    @JsonProperty("text")
    private String text;

    @JsonProperty("phone")
    private String phone;

    @JsonIgnore
    private String tripNum;

    public SendSmsRequest(String text, String phone, String tripNum) {
        this.text = text;
        this.phone = phone;
        this.tripNum = tripNum;
    }

}
