package online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

/**
 * Ошибка преобразования URL'а
 */
public class UrlCutterException extends Exception {
    //extends BadRequestException
    //TODO: catch timeOut-exception?
    @NotNull
    private final HttpStatus status;
    @NotNull
    private final Error errors;

    public UrlCutterException(@NotNull HttpStatus status,
                              @NotNull Error errors) {
        super();
        this.status = status;
        this.errors = errors;
    }

    public @NotNull Error getErrors() {
        return this.errors;
    }

}
