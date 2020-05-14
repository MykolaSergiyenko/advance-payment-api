package online.oboz.trip.trip_carrier_advance_payment_api.util;

import jdk.nashorn.internal.runtime.JSONListAdapter;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

//TODO:AccessUtils
//or this is Token?
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

    public static Long getAuthPersonId() {
        checkState(isAuthenticated());
        Jwt jwt = SecurityUtils.getCurrentToken();
        return Long.parseLong(jwt.getClaimAsMap("person").get("id").toString());
    }

    private static String getAuthPersonEmail() {
        checkState(isAuthenticated());
        Jwt jwt = SecurityUtils.getCurrentToken();

        return jwt.getClaims().get("email").toString();
    }

    private static void checkEmailWithContacts(Jwt jwt, ApplicationProperties props){
        //check with contact dict here?
        StringBuilder s = new StringBuilder("MyClaims:");
        Map<String, Object> map = jwt.getClaims();
                map.forEach((k, v) -> s.append("Печатаем жвт: Key: " + k + "; Value: " + v));
        props.hasAccessUsersEmails().forEach(p ->
            s.append("Печатаем проперти: "+ p));
        s.append("// ?? check with contact dict here ?");
        System.out.println(s.toString());
    }

    private static Jwt getCurrentToken() {
        return ((JwtAuthenticationToken) getAuthentication()).getToken();
    }

    private static boolean hasRole(String role) {
        System.out.println("my role: "+ getCurrentToken().getClaimAsMap("realm_access").get("roles"));
        return ((JSONListAdapter) getCurrentToken().getClaimAsMap("realm_access").get("roles")).contains(role);
        //?
    }

    private static boolean hasAccessEmail(ApplicationProperties applicationProperties){
        //check access by get full accessor-emails list here, in SecurityUtils.
        //filter by "jwt.email"
        String authPerson = getAuthPersonEmail();
        Jwt whatWeHaveAtAll =  SecurityUtils.getCurrentToken();
        //
        checkEmailWithContacts(whatWeHaveAtAll, applicationProperties);
        return applicationProperties.hasAccessUsersEmails().contains(authPerson);
    }

    public static boolean hasAdmin() {
        //check with contact-advance-role here?
        return hasRole("admin");
    }


    public static boolean hasNotAccess(ApplicationProperties applicationProperties){
        return !hasAccessEmail(applicationProperties);
    }
}

