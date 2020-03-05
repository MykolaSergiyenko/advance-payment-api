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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.anonymous().and()
            .csrf().disable()
            .oauth2ResourceServer().jwt().and().and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, AUTH_WHITELIST).permitAll()
            .antMatchers(HttpMethod.POST, AUTH_WHITELIST).permitAll()
            .antMatchers("/v1/**").authenticated();
    }
}
