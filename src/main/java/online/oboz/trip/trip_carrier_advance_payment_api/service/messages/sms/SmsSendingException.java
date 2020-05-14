package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

public class SmsSendingException extends Exception {

    @NotNull
    private final HttpStatus status;
    @NotNull
    private final Error errors;

    public SmsSendingException(@NotNull HttpStatus status,
                               @NotNull Error errors) {
        super();
        this.status = status;
        this.errors = errors;
    }

    public @NotNull Error getErrors() {
        return this.errors;
    }
}
