package online.oboz.trip.trip_carrier_advance_payment_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@ConfigurationProperties(prefix = "git-properties", ignoreUnknownFields = false)
@PropertySources({
    @PropertySource(value = {"classpath:git.properties"}, ignoreResourceNotFound = false),
    @PropertySource(value = {"classpath:META-INF/build-info.properties"}, ignoreResourceNotFound = true)
})
public class GitProperties {
}
