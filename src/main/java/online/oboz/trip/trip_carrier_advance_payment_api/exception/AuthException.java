package online.oboz.trip.trip_carrier_advance_payment_api.exception;

public class AuthException extends RuntimeException {
    public AuthException(String s) {
        super(s);
    }

    public AuthException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
