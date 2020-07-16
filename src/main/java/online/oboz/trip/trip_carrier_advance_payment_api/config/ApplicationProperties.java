package online.oboz.trip.trip_carrier_advance_payment_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

@ConfigurationProperties(prefix = "application", ignoreInvalidFields = false)
public class ApplicationProperties {
    /**
     * Spring mail-sender
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


    @Deprecated
    @Value("${accessed-users.ids}")
    private List<Long> accessUsersIds;


    /**
     * Access-only-emails-list
     */
    @Value("${accessed-users.emails}")
    private List<String> accessUsersEmails;


    /**
     * Keycloak-url in RestService
     */
    @Value("${services.keycloak.url}")
    private URL tokenAuthUrl;


    /**
     * Get keycloak Token
     */
    @Value("${services.keycloak.token-postfix}")
    private String tokenUrlPostfix;


    /**
     * Keycloak request
     */
    @Value("${services.keycloak.token-body}")
    private String tokenBody;

    /**
     * Keycloak username
     */
    @Value("${services.keycloak.auth.username}")
    private String username;

    /**
     * Keycloak password
     */
    @Value("${services.keycloak.auth.password}")
    private String password;


    /**
     * Min cost of trip to be advanced
     */
    @Value("${services.advance-service.min-trip-cost}")
    private Double minTripCost;

    //Auto-advance

    /**
     * Schedule for Auto-advance service created auto-advances
     */
    @Value("${services.auto-advance-service.cron.creation}")
    private String cronCreation;

    /**
     * Schedule for Auto-advance service updated auto-contractors
     * and update advance-attachments-uuid's
     */
    @Value("${services.auto-advance-service.cron.update}")
    private String cronUpdate;

    /**
     * Auto Created-advance comment
     */
    @Value("${services.auto-advance-service.comment}")
    private String autoCreatedComment;


    /**
     * Auto-advances author personId
     */
    @Value("${services.auto-advance-service.auto-author}")
    private Long autoAuthor;

    /**
     * Min-date of trip-assigning for give auto-advance
     */
    @Value("${services.auto-advance-service.min-date}")
    private OffsetDateTime minDate;


    /**
     * Min count of 'paid' advances for contractor to become 'automated'
     */
    @Value("${services.auto-advance-service.min-paid-advance-count}")
    private Long minAdvanceCount;

    /**
     * URL for BStore-service
     */
    @Value("${services.bstore.url}")
    private URL bStoreUrl;

    /**
     * B-store 'pdf'-suffix
     */
    @Value("${services.bstore.pdf}")
    private String bStorePdf;


    /**
     * ReportServer-url for generate 'Advance request template' in PDF
     */
    @Value("${services.reports.url}")
    private URL reportsUrl;


    /**
     * ReportServer - Type-key (preprod-...)
     */
    @Value("${services.reports.type-key}")
    private String reportsTypeKey;

    /**
     * ReportServer - User
     */
    @Value("${services.reports.user}")
    private String reportsUser;

    /**
     * ReportServer - api-key
     */
    @Value("${services.reports.api-key}")
    private String reportsApiKey;

    /**
     * ReportServer - file-format (*.pdf)
     */
    @Value("${services.reports.format}")
    private String reportsFormat;

    /**
     * ReportServer - params of Advance for report-template
     */
    @Value("${services.reports.template-params}")
    private String reportsParams;


    //Notification - Messaging services
    /**
     * Advance Client's (carrier-page) URL
     */
    @Value("${services.notifications.lk-url}")
    private URL lkUrl;

    /**
     * Config-URL for url-CutterService
     */
    @Value("${services.notifications.cut-link-url}")
    private URL cutLinkUrl;


    /**
     * Schedule for "Send SMSs for unread letters-advances"
     */
    @Value("${services.notifications.scheduler.notify}")
    private String cronCheckNotify;


    @Deprecated
    @Value("${services.notifications.scheduler.email-newadvance-interval}")
    private String emailInterval;


    /**
     * Unread letters interval for scheduled notifications - in minutes
     */
    @Value("${services.notifications.scheduler.sms-newadvance-inteval}")
    private Integer smsInterval;


    /**
     * Is email-enabled for create-advance simple-notification in NotificationService
     */
    @Value("${services.notifications.email.email-enable}")
    private Boolean emailEnable;

    /**
     * Is email-enabled for create-advance scheduled-notification in NotificationService.
     * Now is unable
     */
    @Value("${services.notifications.email.email-schedule-enable}")
    private Boolean emailScheduleEnable;


    /**
     * Enable cut-links for emails in NotificationService.
     * Now is unable. Use ShorterService in EmailCreator if enable
     */
    @Value("${services.notifications.email.cut-links}")
    private Boolean emailCutLinks;


    /**
     * Email-message params\templates. Used in message-TextService only.
     */
    @Value("${services.notifications.email.email-header-template}")
    private String emailHeaderTemplate;
    @Value("${services.notifications.email.email-message-template}")
    private String emailMessageTemplate;


    /**
     * Is sms-enabled for create-advance simple-notification in NotificationService
     * Now unable - sms is delayed and send by cron after email-only-notification.
     */
    @Value("${services.notifications.sms.enable}")
    private Boolean smsEnable;

    /**
     * Is sms-enabled for create-advance scheduled-notification in NotificationService.
     * "Delayed"-sms for unread email's enable.
     */
    @Value("${services.notifications.sms.schedule-enable}")
    private Boolean smsScheduleEnable;


    /**
     * Sms-sender-url for SmsSenderService --> via restTemplate
     */
    @Value("${services.notifications.sms.sender-url}")
    private URL smsSenderUrl;

    /**
     * SMS-phone-template and SMS-message-template
     * for TextService in SmsCreator
     */
    @Value("${services.notifications.sms.sms-phone-template}")
    private String smsPhoneTemplate;
    @Value("${services.notifications.sms.sms-message-template}")
    private String smsMessageTemplate;

    /**
     * Enable cut-links for emails in NotificationService.
     * Now is able for short-sms but not in email.
     * Use ShorterService in SmsCreator
     */
    @Value("${services.notifications.sms.cut-links}")
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


    public String getAutoCreatedComment() {
        return autoCreatedComment;
    }

    public void setAutoCreatedComment(String autoCreatedComment) {
        this.autoCreatedComment = autoCreatedComment;
    }


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


    public Long getMinAdvanceCount() {
        return this.minAdvanceCount;
    }

    public void setMinAdvanceCount(Long minAdvanceCount) {
        this.minAdvanceCount = minAdvanceCount;
    }


    public URL getbStoreUrl() {
        return bStoreUrl;
    }

    public void setbStoreUrl(URL bStoreUrl) {
        this.bStoreUrl = bStoreUrl;
    }


    public URL getReportsUrl() {
        return this.reportsUrl;
    }

    public void setReportsUrl(URL reportsUrl) {
        this.reportsUrl = reportsUrl;
    }


    public URL getLkUrl() {
        return this.lkUrl;
    }

    public void setLkUrl(URL lkUrl) {
        this.lkUrl = lkUrl;
    }


    public URL getCutLinkUrl() {
        return cutLinkUrl;
    }

    public void setCutLinkUrl(URL cutLinkUrl) {
        this.cutLinkUrl = cutLinkUrl;
    }


    public String getCronCheckNotify() {
        return cronCheckNotify;
    }

    public void setCronCheckNotify(String cronCheckNotify) {
        this.cronCheckNotify = cronCheckNotify;
    }


//    /**
//     * Unread email's interval for scheduled-emails? NotificationService ?
//     */
//    public String getEmailInterval() {
//        return emailInterval;
//    }
//
//    public void setEmailInterval(String emailInterval) {
//        this.emailInterval = emailInterval;
//    }

    /**
     * Unread email's interval for sms-scheduled-notification NotificationService?
     */
    public Integer getSmsInterval() {
        return smsInterval;
    }

    public void setSmsInterval(Integer smsInterval) {
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

    public Long getAutoAuthor() {
        return autoAuthor;
    }

    public void setAutoAuthor(Long autoAuthor) {
        this.autoAuthor = autoAuthor;
    }


    public OffsetDateTime getMinDate() {
        return minDate;
    }

    public void setMinDate(OffsetDateTime minDate) {
        this.minDate = minDate;
    }


}
