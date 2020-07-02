package online.oboz.trip.trip_carrier_advance_payment_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import springfox.documentation.spring.web.json.Json;

import java.net.URL;
import java.util.List;

@ConfigurationProperties(prefix = "application", ignoreInvalidFields = false)
public class ApplicationProperties {
    /**
     * TODO: check use Spring-mail config here?
     */
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

//
//    /**
//     * accessUsersIds not use
//     */
//    @Deprecated
//    @Value("${accessed-users.ids}")
//    private List<Long> accessUsersIds;


    /**
     * Access-only-emails-list
     */
    @Value("${accessed-users.emails}")
    private List<String> accessUsersEmails;


    /**
     * tokenAuthUrl in RestService
     * //TODO:keyCloakUrl? make different service or config?
     */
    @Value("${services.keycloak.url}")
    private URL tokenAuthUrl;

    @Value("${services.keycloak.token-postfix}")
    private String tokenUrlPostfix;

    @Value("${services.keycloak.token-body}")
    private String tokenBody;

    @Value("${services.keycloak.auth.username}")
    private String username;

    @Value("${services.keycloak.auth.password}")
    private String password;

    //Advance
    // Main-application-services

    /**
     * requiredDownloadDocs
     * Strange property for application
     * //TODO: Get requiredDownloadDocs for activate button in DispatcherService?
     */
    @Value("${services.advance-service.required-docs:true}")
    private Boolean requiredDownloadDocs;



    @Value("${services.advance-service.min-trip-cost}")
    private Double  minTripCost;

    //Auto-advance

    /**
     * cron
     * //TODO: Crons for auto-advance action to check?
     * // created and updated in one?
     */
    @Value("${services.auto-advance-service.cron.creation: 0 0/2 * * * *}")
    private String cronCreation;

    @Value("${services.auto-advance-service.cron.update: 0 0/2 * * * *}")
    private String cronUpdate;

    /**
     * Auto Created-advance comment
     * TODO: Use auto-created-comment from app.properties
     */
    @Value("${services.auto-advance-service.comment}")
    private String autoCreatedComment;


    @Value("${services.auto-advance-service.min-paid-advance-count}")
    private Long minAdvanceCount;

    /**
     * URL for BStore-service
     * TODO: Is make auth-Request via RestService --> RestTemplate ?
     */
    @Value("${services.bstore.url}")
    private URL bStoreUrl;

    @Value("${services.bstore.pdf}")
    private String bStorePdf;

    /**
     * OrdersApi-url for OrdersApiService
     * TODO: Is make auth-Request via RestService --> RestTemplate ?
     */
    @Value("${services.orders.url}")
    private URL ordersApiUrl;

    @Value("${services.orders.save-body}")
    private Json ordersApiSaveBody;

    /**
     * ReportServer-url for OrdersApiService
     * TODO: Is make Request via restService.executeGetAuthRequest() --> RestTemplate ?
     */
    @Value("${services.reports.url}")
    private URL reportsUrl;

    @Value("${services.reports.type-key}")
    private String reportsTypeKey;

    @Value("${services.reports.user}")
    private String reportsUser;

    @Value("${services.reports.api-key}")
    private String reportsApiKey;

    @Value("${services.reports.format}")
    private String reportsFormat;

    @Value("${services.reports.template-params}")
    private String reportsParams;


    //Notification - Messaging services
    /**
     * Client's advance "dashboard-URL" for NotificationService
     * TODO: Make template maybe? //? URL, String
     */
    @Value("${services.notifications.lk-url}")
    private URL lkUrl;

    /**
     * Config-URL for url-CutterService
     */
    @Value("${services.notifications.cut-link-url:https://clck.ru/--?url=}")
    private URL cutLinkUrl;


    /**
     * TODO: set in cron for all messages (sms- and emails)
     */
    @Value("${services.notifications.scheduler.notify:0 0/30 * * * *}")
    String cronCheckNotify;

    /**
     * TODO: Unread email's interval for scheduled-emails? in NotificationService
     */
    @Value("${services.notifications.scheduler.email-newadvance-interval:1 hour}")
    String emailInterval;

    /**
     * TODO: Unread email's interval for sms-scheduled-notification NotificationService?
     * Make logic for intervals - now scheduled-emails unable?
     * TODO:interval? String? #minutes? #hours?
     * //${notification.delay.sms-send:60000
     * milliseconds is too much for all needs
     */
    @Value("${services.notifications.scheduler.sms-newadvance-inteval:2 hours}")
    String smsInterval;


    //Email-notification-config for NotificationService
    /**
     * Is email-enabled for create-advance simple-notification in NotificationService
     */
    @Value("${services.notifications.email.email-enable:true}")
    private Boolean emailEnable;

    /**
     * Is email-enabled for create-advance scheduled-notification in NotificationService.
     * Now is unable
     */
    @Value("${services.notifications.email.email-schedule-enable:true}")
    private Boolean emailScheduleEnable;


    /**
     * Enable cut-links for emails in NotificationService.
     * Now is unable. Use ShorterService in EmailCreator if enable
     */
    @Value("${services.notifications.email.cut-links:false}")
    private Boolean emailCutLinks;

    //Sender not config here. Use by default Spring's?

    /**
     * Email-message params\templates. Used in message-TextService only.
     */
    @Value("${services.notifications.email.email-header-template}")
    private String emailHeaderTemplate;
    @Value("${services.notifications.email.email-message-template}")
    private String emailMessageTemplate;


    //Sms-notification-config for NotificationService
    /**
     * Is sms-enabled for create-advance simple-notification in NotificationService
     * Now unable - sms is delayed and send by cron after email-only-notification.
     */
    @Value("${services.services.notifications.sms.enable:true}")
    private Boolean smsEnable;

    /**
     * Is sms-enabled for create-advance scheduled-notification in NotificationService.
     * "Delayed"-sms for unread email's enable.
     */
    @Value("${services.notifications.sms.schedule-enable:true}")
    private Boolean smsScheduleEnable;


    /**
     * Sms-sender-url for SmsSenderService --> via restTemplate
     * TODO: use via RestService proxy maybe?
     */
    @Value("${services.notifications.sms.sender-url:http://sms-sender.r14.k.preprod.oboz:30080}")
    private URL smsSenderUrl;

    /**
     * SMS-phone-template and SMS-message-template
     * for TextService in SmsCreator
     * TODO: check in SmsCreator?
     */
    @Value("${services.notifications.sms.sms-phone-template:7%s}")
    private String smsPhoneTemplate;
    @Value("${services.notifications.sms.sms-message-template}")
    private String smsMessageTemplate;

    /**
     * Enable cut-links for emails in NotificationService.
     * Now is able for short-sms but not in email.
     * Use ShorterService in SmsCreator
     */
    @Value("${services.notifications.sms.cut-links:true}")
    private Boolean smsCutLinks;

    public ApplicationProperties() {
    }

    /**
     * @return Access-only-emails-list
     */
    public List<String> hasAccessUsersEmails() {
        return this.accessUsersEmails;
    }

    /**
     * @return Set access-only-emails-list
     */
    public void setAccessUsersEmails(List<String> emails) {
        this.accessUsersEmails = emails;
    }

    /**
     * @return Check hasAccess by only-user's emails here,
     * not give all email's list for SecurityUtils.
     * TODO: check by contains() or streams.filter() here
     * by private ApplicationProperties.hasAccess(java.lang.String email)
     * and authenticateUser.emails, ids, other params from front-end;
     */
    @Deprecated
    private boolean hasAccess(String email) {
        return hasAccessUsersEmails().contains(email);
    }


    /**
     * @return Get token URL for RestService.getRequestToken(). keykloak?
     */
    public URL getTokenAuthUrl() {
        return this.tokenAuthUrl;
    }

    public void setTokenAuthUrl(URL tokenAuthUrl) {
        this.tokenAuthUrl = tokenAuthUrl;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Get requiredDownloadDocs for activate button in DispatcherService?
     * //requiredDownloadDocs in app.property??
     * // звучит не очень
     */
    public Boolean getRequiredDownloadDocs() {
        return this.requiredDownloadDocs;
    }

    public void setRequiredDownloadDocs(Boolean requiredDownloadDocs) {
        this.requiredDownloadDocs = requiredDownloadDocs;
    }


    /**
     * TODO: Use auto-comment from app.properties
     */
    public String getAutoCreatedComment() {
        return autoCreatedComment;
    }

    public void setAutoCreatedComment(String autoCreatedComment) {
        this.autoCreatedComment = autoCreatedComment;
    }


    //Config auto-advance properties here. Make some private maybe

    /**
     * Config auto-advance properties here
     */
    public String getCronCreation() {
        return cronCreation;
    }

    public void setCronCreation(String cronCreation) {
        this.cronCreation = cronCreation;
    }

    public String getCronUpdate() {
        return cronUpdate;
    }

    public void setCronUpdate(String cronUpdate) {
        this.cronUpdate = cronUpdate;
    }

    /**
     * Get minimum count, dates of driver's trips for AutoAdvanceService
     */
    public Long getMinAdvanceCount() {
        return this.minAdvanceCount;
    }

    public void setMinAdvanceCount(Long minAdvanceCount) {
        this.minAdvanceCount = minAdvanceCount;
    }



    /**
     * Get B-Store-url for B-StoreService
     * //TODO: Is make auth-Request via RestService --> RestTemplate ?
     */
    public URL getbStoreUrl() {
        return bStoreUrl;
    }

    public void setbStoreUrl(URL bStoreUrl) {
        this.bStoreUrl = bStoreUrl;
    }

    /**
     * Get OrdersApi-url for OrdersApiService
     * //TODO: Used RestService.executePostAuthRequest() --> restTemplate?
     */
    public URL getOrdersApiUrl() {
        return this.ordersApiUrl;
    }

    public void setOrdersApiUrl(URL ordersApiUrl) {
        this.ordersApiUrl = ordersApiUrl;
    }

    /**
     * Get reportServer-url for OrdersApiService
     * //TODO: check how used
     * TODO: - посмотреть костыли в downloadAvanceRequestTemplate
     */
    public URL getReportsUrl() {
        return this.reportsUrl;
    }

    public void setReportsUrl(URL reportsUrl) {
        this.reportsUrl = reportsUrl;
    }


    /**
     * Config client's advance "LK-URL" for NotificationService
     */
    public URL getLkUrl() {
        return this.lkUrl;
    }

    public void setLkUrl(URL lkUrl) {
        this.lkUrl = lkUrl;
    }


    /**
     * Config for UrlCutterService
     */
    public URL getCutLinkUrl() {
        return cutLinkUrl;
    }

    public void setCutLinkUrl(URL cutLinkUrl) {
        this.cutLinkUrl = cutLinkUrl;
    }


    /**
     * Cron for scheduled NotificationService
     */
    public String getCronCheckNotify() {
        return cronCheckNotify;
    }

    public void setCronCheckNotify(String cronCheckNotify) {
        this.cronCheckNotify = cronCheckNotify;
    }


    /**
     * Unread email's interval for scheduled-emails? NotificationService ?
     */
    public String getEmailInterval() {
        return emailInterval;
    }

    public void setEmailInterval(String emailInterval) {
        this.emailInterval = emailInterval;
    }

    /**
     * Unread email's interval for sms-scheduled-notification NotificationService?
     * TODO: Make logic for intervals - now scheduled-emails unable?
     */
    public String getSmsInterval() {
        return smsInterval;
    }

    public void setSmsInterval(String smsInterval) {
        this.smsInterval = smsInterval;
    }


    /**
     * @return Is email-enabled for create-advance simple-notification in NotificationService
     */
    public Boolean isEmailEnabled() {
        return this.emailEnable;
    }

    public void setEmailEnabled(Boolean emailEnable) {
        this.emailEnable = emailEnable;
    }


    /**
     * @return Is email-enabled for create-advance scheduled-notification in NotificationService.
     * Now is unable
     */
    public Boolean isEmailScheduleEnabled() {
        return emailScheduleEnable;
    }

    public void setEmailScheduleEnabled(Boolean emailScheduleEnable) {
        this.emailScheduleEnable = emailScheduleEnable;
    }


    /**
     * @return Enable cut-links for emails in NotificationService.
     * Now is unable. Use ShorterService in EmailCreator if enable
     */
    public Boolean isEmailCutLinks() {
        return emailCutLinks;
    }

    public void setEmailCutLinks(Boolean emailCutLinks) {
        this.emailCutLinks = emailCutLinks;
    }


    /**
     * @return Email-message templates for e-mail notification
     */
    public String getEmailHeaderTemplate() {
        return emailHeaderTemplate;
    }

    public void setEmailHeaderTemplate(String emailHeaderTemplate) {
        this.emailHeaderTemplate = emailHeaderTemplate;
    }

    public String getEmailMessageTemplate() {
        return emailMessageTemplate;
    }

    public void setEmailMessageTemplate(String emailMessageTemplate) {
        this.emailMessageTemplate = emailMessageTemplate;
    }

    /**
     * Get java-mail-properties configurated for Spring
     * //TODO: Check used everywhere?
     * //Make mail-config.java?
     */
    public String getMailHost() {
        return this.mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public int getMailPort() {
        return this.mailPort;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }


    public String getMailUsername() {
        return this.mailUsername;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public String getMailPassword() {
        return this.mailPassword;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public String getPropertiesMailDebug() {
        return this.propertiesMailDebug;
    }

    public void setPropertiesMailDebug(String propertiesMailDebug) {
        this.propertiesMailDebug = propertiesMailDebug;
    }

    public String getMailStarttls() {
        return this.mailStarttls;
    }

    public String getMailAuth() {
        return this.mailAuth;
    }

    public void setMailStarttls(String mailStarttls) {
        this.mailStarttls = mailStarttls;
    }

    public void setMailAuth(String mailAuth) {
        this.mailAuth = mailAuth;
    }


    //Sms-notification-config for Notification Service

    /**
     * @return Is sms-enabled for create-advance simple-notification in NotificationService
     * Now unable - sms is delayed and send by cron after email-only-notification.
     */
    public Boolean isSmsEnabled() {
        return smsEnable;
    }

    public void setSmsEnable(Boolean smsEnable) {
        this.smsEnable = smsEnable;
    }


    /**
     * @return Is sms-enabled for create-advance in scheduled-notification in NotificationService.
     * This enable "delayed"-sms after unread-email-advances by cron.
     */
    public Boolean isSmsScheduleEnabled() {
        return smsScheduleEnable;
    }

    public void setSmsScheduleEnable(Boolean smsScheduleEnable) {
        this.smsScheduleEnable = smsScheduleEnable;
    }

    /**
     * @return Enable cut-links for emails in NotificationService.
     * Now is able for short-sms but not in email.
     * Use ShorterService in SmsCreator
     */
    public Boolean isSmsCutLinks() {
        return smsCutLinks;
    }

    public void setSmsCutLinks(Boolean smsCutLinks) {
        this.smsCutLinks = smsCutLinks;
    }


    /**
     * @return URL for restTemplate in SmsSenderService.
     */
    public URL getSmsSenderUrl() {
        return this.smsSenderUrl;
    }

    public void setSmsSenderUrl(URL smsSenderUrl) {
        this.smsSenderUrl = smsSenderUrl;
    }

    /**
     * @return SMS-phonenumber-template use in SmsCreator?;
     * TODO: use in text-service?
     */
    public String getSmsPhoneTemplate() {
        return smsPhoneTemplate;
    }

    public void setSmsPhoneTemplate(String smsPhoneTemplate) {
        this.smsPhoneTemplate = smsPhoneTemplate;
    }

    /**
     * @return SMS-message-template for TextService in SmsCreator;
     */
    public String getSmsMessageTemplate() {
        return smsMessageTemplate;
    }

    public void setSmsMessageTemplate(String smsMessageTemplate) {
        this.smsMessageTemplate = smsMessageTemplate;
    }


    public Json getOrdersApiSaveBody() {
        return ordersApiSaveBody;
    }

    public void setOrdersApiSaveBody(Json ordersApiSaveBody) {
        this.ordersApiSaveBody = ordersApiSaveBody;
    }


    public String getTokenUrlPostfix() {
        return tokenUrlPostfix;
    }

    public void setTokenUrlPostfix(String tokenUrlPostfix) {
        this.tokenUrlPostfix = tokenUrlPostfix;
    }

    public String getTokenBody() {
        return tokenBody;
    }

    public void setTokenBody(String tokenBody) {
        this.tokenBody = tokenBody;
    }



    public String getbStorePdf() {
        return bStorePdf;
    }

    public void setbStorePdf(String bStorePdf) {
        this.bStorePdf = bStorePdf;
    }

    public String getReportsTypeKey() {
        return reportsTypeKey;
    }

    public void setReportsTypeKey(String reportsTypeKey) {
        this.reportsTypeKey = reportsTypeKey;
    }

    public String getReportsUser() {
        return reportsUser;
    }

    public void setReportsUser(String reportsUser) {
        this.reportsUser = reportsUser;
    }

    public String getReportsApiKey() {
        return reportsApiKey;
    }

    public void setReportsApiKey(String reportsApiKey) {
        this.reportsApiKey = reportsApiKey;
    }

    public String getReportsFormat() {
        return reportsFormat;
    }

    public void setReportsFormat(String reportsFormat) {
        this.reportsFormat = reportsFormat;
    }

    public String getReportsParams() {
        return reportsParams;
    }

    public void setReportsParams(String reportsParams) {
        this.reportsParams = reportsParams;
    }

    public Double getMinTripCost() {
        return minTripCost;
    }

    public void setMinTripCost(Double minTripCost) {
        this.minTripCost = minTripCost;
    }

}
