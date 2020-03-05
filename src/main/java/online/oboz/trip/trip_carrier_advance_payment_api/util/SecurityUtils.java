package online.oboz.trip.trip_carrier_advance_payment_api.util;


import net.minidev.json.JSONArray;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Jwt getCurrentToken() {
        return ((JwtAuthenticationToken) getAuthentication()).getToken();
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean hasAdmin() {
        return hasRole("admin");
    }

    private static boolean hasRole(String role) {
        return ((JSONArray) getCurrentToken().getClaimAsMap("realm_access").get("roles")).contains(role);
    }

    public static boolean isAuthenticated() {
        return getAuthentication().isAuthenticated();
    }

}

