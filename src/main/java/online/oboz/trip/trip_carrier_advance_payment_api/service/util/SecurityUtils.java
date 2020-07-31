package online.oboz.trip.trip_carrier_advance_payment_api.service.util;

import jdk.nashorn.internal.runtime.JSONListAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static com.google.common.base.Preconditions.checkState;
import static online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties.hasAccess;

public interface SecurityUtils {
    Logger log = LoggerFactory.getLogger(SecurityUtils.class);



    static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    static boolean isAuthenticated() {
        return getAuthentication().isAuthenticated();
    }

    static Jwt getCurrentToken() {
        return ((JwtAuthenticationToken) getAuthentication()).getToken();
    }

    static Jwt getToken() {
        checkState(isAuthenticated());
        return getCurrentToken();
    }

    static Long getAuthPersonId() {
        Jwt jwt = getToken();
        return Long.parseLong(jwt.getClaimAsMap("person").get("id").toString());
    }

    static String getAuthPersonEmail() {
        Jwt jwt = getToken();
        return jwt.getClaims().get("email").toString();
    }


    static boolean hasRole(String role) {
        System.out.println("my role: " + getCurrentToken().getClaimAsMap("realm_access").get("roles"));
        return ((JSONListAdapter) getCurrentToken().getClaimAsMap("realm_access").get("roles")).contains(role);
    }


    static boolean hasAdmin() {
        //check with contact-advance-role here?
        return hasRole("admin");
    }

    static boolean hasAccessEmail() {
        String authPerson = getAuthPersonEmail();
        return hasAccess(authPerson);
    }


    static boolean hasNotAccess() {
        return !hasAccessEmail();
    }
}
