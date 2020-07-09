package online.oboz.trip.trip_carrier_advance_payment_api.util;

import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessagingException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public final class ErrorUtils {
    private static final Logger log = LoggerFactory.getLogger(ErrorUtils.class);

    public static MessagingException getMessagingError(String message) {
        return getBadRequestError(message);
    }

    public static BusinessLogicException getInternalError(String message) {
        return getInternalBusinessError(message, INTERNAL_SERVER_ERROR);
    }

    private static MessagingException getBadRequestError(String message) {
        return getBadRequestBusinessError(message, BAD_REQUEST);
    }

    private static BusinessLogicException getInternalBusinessError(String errorMessage, HttpStatus state) {
        Error error = new Error();
        error.setStatus(state.toString());
        error.setErrorCode(Integer.toString(state.value()));
        error.setErrorMessage(errorMessage);
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }


    private static MessagingException getBadRequestBusinessError(String message, HttpStatus state) {
        Error error = new Error();
        HttpStatus status = state;
        error.setStatus(state.toString());
        error.setErrorCode(Integer.toString(state.value()));
        error.setErrorMessage(message);
        log.error(status.name() + " : " + error.getErrorMessage());
        return new MessagingException(status, error);
    }
}
