package online.oboz.trip.trip_carrier_advance_payment_api.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendSmsRequest {
    @JsonProperty("text")
    private String text;

    @JsonProperty("phone")
    private String phone;

    public SendSmsRequest(String text, String phone) {
        this.text = text;
        this.phone = phone;
    }

}
