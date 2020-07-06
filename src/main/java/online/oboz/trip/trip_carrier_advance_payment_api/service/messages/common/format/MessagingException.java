package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

public class MessagingException extends Exception {

    @NotNull
    private final HttpStatus status;
    @NotNull
    private final Error errors;

    public MessagingException(@NotNull HttpStatus status,
                              @NotNull Error errors) {
        super();
        this.status = status;
        this.errors = errors;
    }

    public @NotNull HttpStatus getStatus() {
        return this.status;
    }

    public @NotNull Error getErrors() {
        return this.errors;
    }

}
