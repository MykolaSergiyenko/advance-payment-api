package online.oboz.trip.trip_carrier_advance_payment_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] AUTH_WHITELIST = {
        "/",
        "/swagger-resources/**",
        "/swagger-ui.html",
        "/v1/api-docs",
        "/management/info",
        "/management",
        "/management/health",
        "/webjars/**",
        "/api-docs/"
    };

    //TODO: configure email white-list here?

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
            .antMatchers(HttpMethod.GET, AUTH_WHITELIST).permitAll()
            .antMatchers(HttpMethod.POST, AUTH_WHITELIST).permitAll()
            .antMatchers(HttpMethod.GET, "/v1/advance_carrier/**").permitAll()
            .antMatchers(HttpMethod.POST, "/v1/advance_carrier/**").permitAll()
            .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();


//            .antMatchers(HttpMethod.GET, "/v1/trip_advance/**").permitAll()
//            .antMatchers(HttpMethod.POST, "/v1/trip_advance/**").permitAll()
//            .antMatchers(HttpMethod.POST, "/v1/advances/**").permitAll()
//            .antMatchers(HttpMethod.PUT, "/v1/advances/**").permitAll()
//            .antMatchers(HttpMethod.GET, "/v1/advance_contacts/**").permitAll()
//            .antMatchers(HttpMethod.POST, "/v1/advance_contacts/**").permitAll()
//            .antMatchers(HttpMethod.PUT, "/v1/advance_contacts/**").permitAll()
    }
}
