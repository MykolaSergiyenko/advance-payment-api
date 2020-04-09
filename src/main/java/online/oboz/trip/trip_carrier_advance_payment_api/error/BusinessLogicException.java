package online.oboz.trip.trip_carrier_advance_payment_api.error;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

public final class BusinessLogicException extends RuntimeException {

    @NotNull
    private final HttpStatus status;
    @NotNull
    private final Error errors;

    public BusinessLogicException(@NotNull HttpStatus status,
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
