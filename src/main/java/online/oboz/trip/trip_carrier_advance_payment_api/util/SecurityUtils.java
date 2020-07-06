package online.oboz.trip.trip_carrier_advance_payment_api.util;

import jdk.nashorn.internal.runtime.JSONListAdapter;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


import static com.google.common.base.Preconditions.checkState;

public final class SecurityUtils {
    Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {
    }


    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static boolean isAuthenticated() {
        return getAuthentication().isAuthenticated();
    }

    private static Jwt getCurrentToken() {
        return ((JwtAuthenticationToken) getAuthentication()).getToken();
    }

    private static Jwt getToken() {
        checkState(isAuthenticated());
        return getCurrentToken();
    }

    public static Long getAuthPersonId() {
        Jwt jwt = getToken();
        return Long.parseLong(jwt.getClaimAsMap("person").get("id").toString());
    }

    private static String getAuthPersonEmail() {
        Jwt jwt = getToken();
        return jwt.getClaims().get("email").toString();
    }


    private static boolean hasRole(String role) {
        System.out.println("my role: " + getCurrentToken().getClaimAsMap("realm_access").get("roles"));
        return ((JSONListAdapter) getCurrentToken().getClaimAsMap("realm_access").get("roles")).contains(role);
    }


    public static boolean hasAdmin() {
        //check with contact-advance-role here?
        return hasRole("admin");
    }

    private static boolean hasAccessEmail(ApplicationProperties applicationProperties) {
        String authPerson = getAuthPersonEmail();
        return applicationProperties.hasAccessUsersEmails().contains(authPerson);
    }


    public static boolean hasNotAccess(ApplicationProperties applicationProperties) {
        return !hasAccessEmail(applicationProperties);
    }
}

