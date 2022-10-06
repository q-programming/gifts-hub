package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEvent;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.schedule.AppEventType;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.Utils;
import freemarker.template.Configuration;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.settings.Settings.*;

@Service
public class MailService {

    private static final String APPLICATION = "application";
    private static final String NAME = "name";
    private static final String ACCOUNT_MAP = "accountsMap";
    private static final String ACCOUNTS = "accounts";
    private static final String EVENTS = "events";
    private static final String GROUP_NAME = "groupName";
    private static final String CONFIRM_LINK = "confirmLink";
    private static final String REGISTER_LINK = "registerLink";
    private static final String LOGO_PNG = "logo.png";
    private static final String AVATAR = "avatar_";
    private static final String USER_AVATAR_PNG = "userAvatar.png";
    private static final String PNG = "png";
    private static final String PUBLIC_LINK = "publicLink";
    private static final String LIST_LINK = "listLink";
    private static final String GIFT = "gift";
    public static final String KID_NAME = "kidName";
    public static final String OWNER = "owner";
    public static final String GIFTS_HUB = "Gifts Hub";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    JavaMailSender mailSender;
    private final PropertyService propertyService;
    private final Configuration freemarkerConfiguration;
    private final MessagesService msgSrv;
    private DataBasePropertySource dbSource;
    private final AccountService accountService;
    private final GiftService giftService;
    private final AppEventService eventService;
    private final Map<Account, File> avatarBuffer;
    private final String newsletter_cron_scheduler;
    private String birthday_cron_scheduler;


    @Autowired
    public MailService(PropertyService propertyService,
                       @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration,
                       MessagesService msgSrv,
                       DataBasePropertySource dataBasePropertySource,
                       AccountService accountService,
                       GiftService giftService,
                       AppEventService eventService,
                       @Value("${app.newsletter.schedule}") String cron,
                       @Value("${app.newsletter.birthday}") String birthdayCron
    ) {
        this.propertyService = propertyService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.msgSrv = msgSrv;
        this.dbSource = dataBasePropertySource;
        this.accountService = accountService;
        this.eventService = eventService;
        avatarBuffer = new HashMap<>();
        this.giftService = giftService;
        this.newsletter_cron_scheduler = cron;
        this.birthday_cron_scheduler = birthdayCron;
        initMailSender();
        schedulerLookup(cron, "Newsletter");
        schedulerLookup(birthdayCron, "Birthday reminders");
    }

    public void initMailSender() {
        JavaMailSenderImpl jmsi = new JavaMailSenderImpl();
        jmsi.setHost(propertyService.getProperty(APP_EMAIL_HOST));
        try {
            jmsi.setPort(Integer.parseInt(propertyService.getProperty(APP_EMAIL_PORT)));
        } catch (NumberFormatException e) {
            LOG.warn("Failed to set port from properties. Default 25 used");
            jmsi.setPort(25);
        }
        jmsi.setUsername(propertyService.getProperty(APP_EMAIL_USERNAME));
        jmsi.setPassword(propertyService.getProperty(APP_EMAIL_PASS));
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        jmsi.setJavaMailProperties(javaMailProperties);
        mailSender = jmsi;
    }

    private void schedulerLookup(String cronString, String type) {
        org.springframework.scheduling.support.CronTrigger trigger =
                new CronTrigger(cronString);
        Calendar todayCal = Calendar.getInstance();
        final Date today = todayCal.getTime();
        Date nextExecutionTime = trigger.nextExecutionTime(
                new TriggerContext() {
                    @Override
                    public Date lastScheduledExecutionTime() {
                        return today;
                    }

                    @Override
                    public Date lastActualExecutionTime() {
                        return today;
                    }

                    @Override
                    public Date lastCompletionTime() {
                        return today;
                    }
                });
        LOG.info("Next scheduled email sending for {} is : {}", type, Utils.convertDateTimeToString(nextExecutionTime));
    }

    /**
     * Test current connection
     *
     * @return true if everything is ok, false if connection is down
     */
    public boolean testConnection() {
        try {
            ((JavaMailSenderImpl) mailSender).testConnection();
        } catch (MessagingException e) {
            LOG.error("SMTP server {}:{} is not responding", ((JavaMailSenderImpl) mailSender).getHost(), ((JavaMailSenderImpl) mailSender).getPort());
            return false;
        }
        return true;
    }

    /**
     * Test if connection is correct. If there are some errors MessagingException will be thrown which should be catched
     *
     * @param host     SMTP host
     * @param port     SMTP port
     * @param username SMTP username
     * @param password SMTP password
     * @throws MessagingException If connection is not established
     */
    public void testConnection(String host, Integer port, String username, String password) throws MessagingException {
        JavaMailSenderImpl jmsi = new JavaMailSenderImpl();
        jmsi.setHost(host);
        jmsi.setPort(port);
        jmsi.setUsername(username);
        jmsi.setPassword(password);
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        jmsi.setJavaMailProperties(javaMailProperties);
        jmsi.testConnection();
    }

    //TODO to be removed
    @Deprecated
    public void sendEmail(Mail mail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setSubject(mail.getMailSubject());
        mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.setMailContent(geContentFromTemplate(mail.getModel(), "emailTemplate.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        LOG.info("Sending email to {}", mail.getMailTo());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    private String geContentFromTemplate(Map<String, Object> model, String emailTemplate) {
        StringBuilder content = new StringBuilder();
        try {
            content.append(FreeMarkerTemplateUtils
                    .processTemplateIntoString(freemarkerConfiguration.getTemplate(emailTemplate), model));
        } catch (Exception e) {
            LOG.error("Error while getting template for {}.{}", emailTemplate, e);
        }
        return content.toString();
    }

    /**
     * Send public gift list to list of emails
     *
     * @param emails email list
     * @throws MessagingException if there were errors while sending email
     */
    public void shareGiftList(List<Mail> emails) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        String publicLink = application + "#/public/" + Utils.getCurrentAccountId();
        for (Mail mail : emails) {
            Locale locale = getMailLocale(mail);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
            mimeMessageHelper.setSubject(msgSrv.getMessage("gift.share.subject", new Object[]{Utils.getCurrentAccount().getFullname()}, "", locale));
            mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
            mimeMessageHelper.setTo(mail.getMailTo());
            mail.addToModel(PUBLIC_LINK, publicLink);
            mail.addToModel(APPLICATION, application);
            mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/giftList.ftl"));
            mimeMessageHelper.setText(mail.getMailContent(), true);
            addAppLogo(mimeMessageHelper);
            File avatarTempFile = getUserAvatar(Utils.getCurrentAccount());
            mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
            mailSender.send(mimeMessageHelper.getMimeMessage());
        }
    }

    /**
     * Send public gift list to list of emails
     *
     * @param gift gift which was removed
     * @throws MessagingException if there were errors while sending email
     */
    public void notifyAboutGiftRemoved(Gift gift) throws MessagingException, AccountNotFoundException, UnsupportedEncodingException {
        Account owner = accountService.findById(gift.getUserId());
        Mail mail = Utils.createMail(gift.getClaimed(), owner);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        String listLink = application + "#/list/" + Utils.getCurrentAccount().getUsername();
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("gift.delete.notify", new Object[]{owner.getFullname()}, "", locale));
        mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(LIST_LINK, listLink);
        mail.addToModel(GIFT, gift.getName());
        mail.addToModel(APPLICATION, application);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/giftRemoved.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        addAppLogo(mimeMessageHelper);
        File avatarTempFile = getUserAvatar(owner);
        mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }


    /**
     * Get resized user avatar and store it as temporary file deleted on server restart
     * Once retrieved it will be stored in avatar buffer for future usages
     *
     * @param account account for which avatar should be resized and retireved
     * @return resized avatar stored in temporary file
     */
    private File getUserAvatar(Account account) {
        File avatarTempFile = avatarBuffer.get(account);
        try {
            if (avatarTempFile == null) {
                BufferedImage originalImage;
                Avatar accountAvatar = accountService.getAccountAvatar(account);
                if (accountAvatar != null) {
                    InputStream is = new ByteArrayInputStream(accountAvatar.getImage());
                    originalImage = ImageIO.read(is);
                } else {
                    File avatarFile = new ClassPathResource("static/images/avatar-placeholder.png").getFile();
                    originalImage = ImageIO.read(avatarFile);
                }
                BufferedImage scaledImg = Scalr.resize(originalImage, 50);
                avatarTempFile = File.createTempFile(account.getId(), ".png");
                avatarTempFile.deleteOnExit();
                ImageIO.write(scaledImg, PNG, avatarTempFile);
                avatarBuffer.put(account, avatarTempFile);
            }
        } catch (IOException e) {
            LOG.error("Failed to properly resize image. {}", e.getMessage());
        }
        return avatarTempFile;
    }

    private Locale getMailLocale(Mail mail) {
        return mail.getLocale() != null ? new Locale(mail.getLocale()) : new Locale(propertyService.getProperty(APP_DEFAULT_LANG));
    }


    /**
     * Send confirmation email to account
     *
     * @param mail  Recipient mail
     * @param event Event which requires confirmation
     * @throws MessagingException if there were errors while sending email
     */
    public void sendConfirmMail(Mail mail, AccountEvent event) throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper mimeMessageHelper = createBaseMimeMessage(mail);
        Locale locale = getMailLocale(mail);
        String confirmLink = mail.getModel().get(APPLICATION) + "#/confirm/" + event.getToken();
        mail.addToModel(CONFIRM_LINK, confirmLink);
        if (Utils.getCurrentAccount() != null) {
            mail.addToModel(OWNER, Utils.getCurrentAccount().getFullname());
        }
        switch (event.getType()) {
            case GROUP_MEMEBER:
                templateGroup(mail, event, mimeMessageHelper, locale, "user.group.invite", "/groupInvite.ftl");
                break;
            case GROUP_ADMIN:
                templateGroup(mail, event, mimeMessageHelper, locale, "user.group.admin", "/groupAdmin.ftl");
                break;
            case GROUP_KID:
                templateKidAddGroup(mail, event, mimeMessageHelper, locale);
                break;
            case ACCOUNT_CONFIRM:
                templateAccountConfirm(mail, mimeMessageHelper, locale);
                break;
            case PASSWORD_RESET:
                templatePasswordReset(mail, event, mimeMessageHelper, locale);
                break;
        }
        if (Utils.getCurrentAccount() != null) {
            File avatarTempFile = getUserAvatar(Utils.getCurrentAccount());
            mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
        } else {
            mimeMessageHelper.addInline(USER_AVATAR_PNG, getUserAvatar(event.getAccount()));
        }
        addAppLogo(mimeMessageHelper);
        LOG.info("Sending confirm message to {}", mail.getMailTo());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    private void templateAccountConfirm(Mail mail, MimeMessageHelper mimeMessageHelper, Locale locale) throws MessagingException {
        mimeMessageHelper.setSubject(msgSrv.getMessage("user.register.confirm", null, "", locale));
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/confirmAccount.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
    }

    private void templatePasswordReset(Mail mail, AccountEvent event, MimeMessageHelper mimeMessageHelper, Locale locale) throws MessagingException {
        String confirmLink = mail.getModel().get(APPLICATION) + "#/password-change/" + event.getToken();
        mail.getModel().put(CONFIRM_LINK, confirmLink);
        mimeMessageHelper.setSubject(msgSrv.getMessage("user.login.reset", null, "", locale));
        if (event.getAccount().getType().equals(AccountType.GOOGLE)) {
            mail.addToModel("linkGoogle", mail.getModel().get(APPLICATION) + "login/google");
        } else if (event.getAccount().getType().equals(AccountType.FACEBOOK)) {
            mail.addToModel("linkFacebook", mail.getModel().get(APPLICATION) + "login/facebook");
        }
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/passwordReset.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        //include buttons
        if (event.getAccount().getType().equals(AccountType.GOOGLE)) {
            mimeMessageHelper.addInline("signInGoogle.png", new ClassPathResource("static/images/signin_google_" + locale.toString() + ".png"));
        } else if (event.getAccount().getType().equals(AccountType.FACEBOOK)) {
            mimeMessageHelper.addInline("signInFacebook.png", new ClassPathResource("static/images/signin_facebook_" + locale.toString() + ".png"));
        }
    }

    private void templateGroup(Mail mail, AccountEvent event, MimeMessageHelper mimeMessageHelper, Locale locale, String subjectKey, String template) throws MessagingException {
        String groupName = event.getGroup().getName();
        mimeMessageHelper.setSubject(msgSrv.getMessage(subjectKey, new Object[]{groupName}, "", locale));
        mail.addToModel(GROUP_NAME, groupName);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + template));
        mimeMessageHelper.setText(mail.getMailContent(), true);
    }

    private void templateKidAddGroup(Mail mail, AccountEvent event, MimeMessageHelper mimeMessageHelper, Locale locale) throws MessagingException {
        String groupName = event.getGroup().getName();
        mimeMessageHelper.setSubject(msgSrv.getMessage("user.group.kid.add", null, "", locale));
        mail.addToModel(GROUP_NAME, groupName);
        mail.addToModel(KID_NAME, event.getAccount().getFullname());
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/groupKidAdd.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
    }

    private MimeMessageHelper createBaseMimeMessage(Mail mail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        return mimeMessageHelper;
    }

    public void sendInvite(Mail mail, String familyName) throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper mimeMessageHelper = createBaseMimeMessage(mail);
        Locale locale = getMailLocale(mail);
        mimeMessageHelper.setSubject(msgSrv.getMessage("user.group.invite", new Object[]{familyName}, "", locale));
        String confirmLink = mail.getModel().get(APPLICATION) + "#/register/";
        mail.addToModel(REGISTER_LINK, confirmLink);
        mail.addToModel(GROUP_NAME, familyName);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/groupInvite.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        LOG.info("Sending invite message to {}", mail.getMailTo());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    /**
     * Send all scheduled events. While sending email to account it's events are omitted
     * All events are purged afterwards ( marked as processed )
     *
     * @throws MessagingException if there were errors while sending email
     */
    @Scheduled(cron = "${app.newsletter.schedule}")
    @Transactional
    public void sendEvents() throws MessagingException, UnsupportedEncodingException {
        LOG.info("Begin sending scheduled newsletter");
        int mailsSent = 0;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        Map<Account, List<AppEvent>> eventMap = eventService.getEventsGroupedByAccount();
        Set<Group> groups = eventMap
                .keySet()
                .stream()
                .map(Account::getGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        val newsletterEnabledAccountsFromGroup = getNewsletterAccountsFromGroups(groups);
        for (Account recipientAccount : newsletterEnabledAccountsFromGroup) {
            Map<Account, List<AppEvent>> eventsWithoutRecipient = filterEventMap(eventMap, recipientAccount);
            if (!eventsWithoutRecipient.isEmpty()) {
                sendEventForAccount(mimeMessage, application, eventsWithoutRecipient, recipientAccount);
                mailsSent++;
            }
        }
        eventService.processEvents();
        LOG.info("Newsletter sent to {} recipients", mailsSent);
    }

    private Set<Account> getNewsletterAccountsFromGroups(Set<Group> groups) {
        return groups
                .stream()
                .map(Group::getMembers)
                .flatMap(Collection::stream)
                .filter(account -> StringUtils.isNotBlank(account.getEmail()))
                .filter(Account::getNotifications)
                .collect(Collectors.toSet());
    }

    /**
     * Sends birthday reminder  to all group members for accounts that have birthday soon ( based on application settings )
     * @throws MessagingException if there were errors while sending email
     */
    @Scheduled(cron = "${app.newsletter.birthday}")
    @Transactional
    public void sendBirthDayReminders() throws MessagingException, UnsupportedEncodingException {
        List<Account> withBirthdaySoon = accountService.findWithBirthdaySoon();
        if (withBirthdaySoon.size() > 0) {
            withBirthdaySoon.forEach(account -> LOG.info("{} will have birthday soon!", account.getName()));
            val groups = withBirthdaySoon
                    .stream()
                    .map(Account::getGroups)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            val newsletterAccounts = getNewsletterAccountsFromGroups(groups);
            int mailsSent = 0;
            for (Account emailAccount : newsletterAccounts) {
                mailsSent = sendBirthdayReminderOnlyForGroupMembers(withBirthdaySoon, emailAccount, mailsSent);
            }
            LOG.info("Birthday reminder sent to {} recipients", mailsSent);
        }
    }

    private int sendBirthdayReminderOnlyForGroupMembers(List<Account> withBirthdaySoon, Account emailAccount, int count) throws MessagingException, UnsupportedEncodingException {
        val membersFromGroup = emailAccount.getGroups()
                .stream()
                .map(Group::getMembers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        val filteredWithBirthday = withBirthdaySoon
                .stream()
                .filter(membersFromGroup::contains)
                .collect(Collectors.toList());
        if (filteredWithBirthday.size() > 0) {
            Map<Account, List<Gift>> giftMap = new HashMap<>();
            val application = propertyService.getProperty(APP_URL);
            filteredWithBirthday.forEach(account -> {
                val allClaimedByUser = giftService.findAllClaimedByUser(emailAccount, account.getId());
                if (emailAccount != account) {
                    giftMap.put(account, allClaimedByUser);
                }
            });
            if (giftMap.keySet().size() > 0) {
                val mimeMessage = mailSender.createMimeMessage();
                sendBirthdayReminder(giftMap, mimeMessage, application, emailAccount);
                count++;
            }
        }
        return count;
    }

    private void sendBirthdayReminder(Map<Account, List<Gift>> giftMap, MimeMessage mimeMessage, String application, Account emailAccount) throws MessagingException, UnsupportedEncodingException {
        val mail = Utils.createMail(emailAccount);
        val from = propertyService.getProperty(APP_EMAIL_FROM);
        val locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("schedule.birthday.summary", null, "", locale));
        mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        mail.addToModel(NAME, emailAccount.getName());
        mail.addToModel(ACCOUNT_MAP, giftMap);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/birthday.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        addAppLogo(mimeMessageHelper);
        addAccountsAvatars(giftMap.keySet(), mimeMessageHelper);
        LOG.debug("Sending scheduled email to {}", emailAccount.getEmail());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    /**
     * Filter-out events which are not from recipients groups , or are regarding recipient
     *
     * @param eventMap         map containing Account List of AppEvents
     * @param recipientAccount account who should receive mail
     * @return Filtered events
     */
    private Map<Account, List<AppEvent>> filterEventMap(Map<Account, List<AppEvent>> eventMap, Account recipientAccount) {
        Set<Account> membersFromRecipientGroups = recipientAccount.getGroups().stream().map(Group::getMembers).flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Map<Account, List<AppEvent>> eventsWithoutRecipient = eventMap.entrySet().stream()
                .filter(eventAccount -> membersFromRecipientGroups.contains(eventAccount.getKey()) && !eventAccount.getKey().equals(recipientAccount))
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> new ArrayList<>(filterEventList(entry.getValue(), recipientAccount))));
        return eventsWithoutRecipient.entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Filter out all events which belongs to recipient of email
     *
     * @param list list of events
     * @return filtered out list
     */
    private List<AppEvent> filterEventList(List<AppEvent> list, Account recipientAccount) {
        Map<Gift, List<AppEvent>> giftEventsMap = list.stream().collect(Collectors.groupingBy(AppEvent::getGift));
        return list
                .stream()
                .filter(appEvent -> filterCreatedByEvents(appEvent, recipientAccount))
                .filter(appEvent -> filterSameGiftEvents(appEvent, giftEventsMap.get(appEvent.getGift())))
                .collect(Collectors.toList());
    }

    private boolean filterCreatedByEvents(AppEvent appEvent, Account recipientAccount) {
        return appEvent.getCreatedBy() == null || !recipientAccount.equals(appEvent.getCreatedBy());
    }

    private boolean filterSameGiftEvents(AppEvent appEvent, List<AppEvent> list) {
        Set<AppEventType> giftEvents = list.stream().map(AppEvent::getType).collect(Collectors.toSet());
        if (giftEvents.size() > 1 && AppEventType.NEW.equals(appEvent.getType())) {
            return !giftEvents.contains(AppEventType.REALISED);
        }
        return true;
    }

    /**
     * Form event into email and send it to account recipient
     *
     * @param mimeMessage message to be sent
     * @param application application url
     * @param eventMap    map with all events
     * @param account     account which will recieve email
     * @throws MessagingException if there were errors while sending email
     */
    private void sendEventForAccount(MimeMessage mimeMessage, String application, Map<Account, List<AppEvent>> eventMap, Account account) throws MessagingException, UnsupportedEncodingException {
        Mail mail = Utils.createMail(account);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("schedule.event.summary", null, "", locale));
        mimeMessageHelper.setFrom(new InternetAddress(from, GIFTS_HUB));
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        mail.addToModel(NAME, account.getName());
        mail.addToModel(EVENTS, eventMap);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/scheduler.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        addAppLogo(mimeMessageHelper);
        addAccountsAvatars(eventMap.keySet(), mimeMessageHelper);
        LOG.debug("Sending scheduled email to {}", account.getEmail());

        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    private void addAccountsAvatars(Set<Account> accountList, MimeMessageHelper mimeMessageHelper) throws MessagingException {
        for (Account account : accountList) {
            mimeMessageHelper.addInline(AVATAR + account.getId(), getUserAvatar(account));
        }
    }

    private void addAppLogo(MimeMessageHelper mimeMessageHelper) throws MessagingException {
        mimeMessageHelper.addInline(LOGO_PNG, new ClassPathResource("static/images/logo_email.png"));
    }
}
