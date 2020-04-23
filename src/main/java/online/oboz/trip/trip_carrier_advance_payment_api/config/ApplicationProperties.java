package online.oboz.trip.trip_carrier_advance_payment_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.OffsetDateTime;

@ConfigurationProperties(prefix = "application", ignoreInvalidFields = false)
public class ApplicationProperties {

    @Value("${sms-sender.url:http://sms-sender.r14.k.preprod.oboz:30080}")
    private String smsSenderUrl;
    @Value("${cut-link.url:https://clck.ru/--?url=}")
    private String cutLinkUrl;
    @Value("${auto-advance.min-count-trip:3}")
    private Integer minCountTrip;
    @Value("${auto-advance.min-date-trip:2020-01-01T00:00:00+00:00}")
    private String strMinDateTrip;
    private OffsetDateTime minDateTrip;
    @Value("${required-download-docs:true}")
    private Boolean requiredDownloadDocs;
    private String mailHost;
    @Value("${spring.mail.port}")
    private int mailPort = 587;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;
    @Value("${spring.mail.properties.mail.debug:false}")
    private String propertiesMailDebug;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String mailStarttls;
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String mailAuth;
    @Value("${notification.lk-url}")
    private String lkUrl;
    @Value("${notification.email-enable:true}")
    private Boolean mailEnable;
    @Value("${services.keycloak.auth.username}")
    private String username;
    @Value("${services.keycloak.auth.password}")
    private String password;
    @Value("${services.bstore-url}")
    private String bStoreUrl;
    @Value("${services.orders-api-url}")
    private String ordersApiUrl;
    @Value("${services.report-server-url}")
    private String reportServerUrl;
    @Value("${services.keycloak.url}")
    private String tokenAuthUrl;
    @Value("${notification.delay.sms-send:60000}")
    private Integer smsSendDelay;

    public ApplicationProperties() {
    }

    public OffsetDateTime getMinDateTrip() {
        return OffsetDateTime.parse(strMinDateTrip);
    }

    public String getSmsSenderUrl() {
        return this.smsSenderUrl;
    }

    public String getCutLinkUrl() {
        return cutLinkUrl;
    }

    public Integer getMinCountTrip() {
        return this.minCountTrip;
    }

    public String getStrMinDateTrip() {
        return this.strMinDateTrip;
    }

    public Boolean getRequiredDownloadDocs() {
        return this.requiredDownloadDocs;
    }

    public String getMailHost() {
        return this.mailHost;
    }

    public int getMailPort() {
        return this.mailPort;
    }

    public String getMailUsername() {
        return this.mailUsername;
    }

    public String getMailPassword() {
        return this.mailPassword;
    }

    public String getPropertiesMailDebug() {
        return this.propertiesMailDebug;
    }

    public String getMailStarttls() {
        return this.mailStarttls;
    }

    public String getMailAuth() {
        return this.mailAuth;
    }

    public String getLkUrl() {
        return this.lkUrl;
    }

    public Boolean getMailEnable() {
        return this.mailEnable;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getBStoreUrl() {
        return this.bStoreUrl;
    }

    public String getOrdersApiUrl() {
        return this.ordersApiUrl;
    }

    public String getReportServerUrl() {
        return this.reportServerUrl;
    }

    public String getTokenAuthUrl() {
        return this.tokenAuthUrl;
    }

    public Integer getSmsSendDelay() {
        return this.smsSendDelay;
    }

    public void setSmsSenderUrl(String smsSenderUrl) {
        this.smsSenderUrl = smsSenderUrl;
    }

    public void setCutLinkUrl(String cutLinkUrl) {
        this.cutLinkUrl = cutLinkUrl;
    }

    public void setStrMinDateTrip(String strMinDateTrip) {
        this.strMinDateTrip = strMinDateTrip;
    }

    public void setMinDateTrip(OffsetDateTime minDateTrip) {
        this.minDateTrip = minDateTrip;
    }

    public void setRequiredDownloadDocs(Boolean requiredDownloadDocs) {
        this.requiredDownloadDocs = requiredDownloadDocs;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public void setPropertiesMailDebug(String propertiesMailDebug) {
        this.propertiesMailDebug = propertiesMailDebug;
    }

    public void setMailStarttls(String mailStarttls) {
        this.mailStarttls = mailStarttls;
    }

    public void setMailAuth(String mailAuth) {
        this.mailAuth = mailAuth;
    }

    public void setLkUrl(String lkUrl) {
        this.lkUrl = lkUrl;
    }

    public void setMailEnable(Boolean mailEnable) {
        this.mailEnable = mailEnable;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBStoreUrl(String bStoreUrl) {
        this.bStoreUrl = bStoreUrl;
    }

    public void setOrdersApiUrl(String ordersApiUrl) {
        this.ordersApiUrl = ordersApiUrl;
    }

    public void setReportServerUrl(String reportServerUrl) {
        this.reportServerUrl = reportServerUrl;
    }

    public void setTokenAuthUrl(String tokenAuthUrl) {
        this.tokenAuthUrl = tokenAuthUrl;
    }

    public void setSmsSendDelay(Integer smsSendDelay) {
        this.smsSendDelay = smsSendDelay;
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
            "smsSenderUrl='" + smsSenderUrl + '\'' +
            ", cutLinkUrl='" + cutLinkUrl + '\'' +
            ", minCountTrip=" + minCountTrip +
            ", strMinDateTrip='" + strMinDateTrip + '\'' +
            ", minDateTrip=" + minDateTrip +
            ", requiredDownloadDocs=" + requiredDownloadDocs +
            ", mailHost='" + mailHost + '\'' +
            ", mailPort=" + mailPort +
            ", mailUsername='" + mailUsername + '\'' +
            ", mailPassword='" + mailPassword + '\'' +
            ", propertiesMailDebug='" + propertiesMailDebug + '\'' +
            ", mailStarttls='" + mailStarttls + '\'' +
            ", mailAuth='" + mailAuth + '\'' +
            ", lkUrl='" + lkUrl + '\'' +
            ", mailEnable=" + mailEnable +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", bStoreUrl='" + bStoreUrl + '\'' +
            ", ordersApiUrl='" + ordersApiUrl + '\'' +
            ", reportServerUrl='" + reportServerUrl + '\'' +
            ", tokenAuthUrl='" + tokenAuthUrl + '\'' +
            ", smsSendDelay=" + smsSendDelay +
            '}';
    }
}
