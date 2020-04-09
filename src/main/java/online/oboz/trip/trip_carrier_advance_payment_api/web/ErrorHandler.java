package online.oboz.trip.trip_carrier_advance_payment_api.web;

import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.dto.DefaultErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    public ResponseEntity<DefaultErrorResult> handleServerError(Exception ex, WebRequest request) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
            new DefaultErrorResult(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<DefaultErrorResult> handleResponseStatusException(
        ResponseStatusException ex,
        WebRequest request
    ) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
            new DefaultErrorResult(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()),
            new HttpHeaders(),
            ex.getStatus()
        );
    }

    @ExceptionHandler({BusinessLogicException.class})
    public ResponseEntity<Error> handleBussinessLogicException(BusinessLogicException ex, WebRequest request) {
        Error receiver = ex.getErrors();
        return new ResponseEntity<>(receiver, new HttpHeaders(), ex.getStatus());
    }

}




