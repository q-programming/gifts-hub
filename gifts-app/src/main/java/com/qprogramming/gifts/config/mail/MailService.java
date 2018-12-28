package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEvent;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.Utils;
import freemarker.template.Configuration;
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
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.settings.Settings.*;

@Service
public class MailService {

    private static final String APPLICATION = "application";
    private static final String NAME = "name";
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
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    JavaMailSender mailSender;
    private PropertyService propertyService;
    private Configuration freemarkerConfiguration;
    private MessagesService msgSrv;
    private DataBasePropertySource dbSource;
    private AccountService accountService;
    private AppEventService eventService;
    private Map<Account, File> avatarBuffer;
    private String cron_scheduler;


    @Autowired
    public MailService(PropertyService propertyService,
                       @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration,
                       MessagesService msgSrv,
                       DataBasePropertySource dataBasePropertySource,
                       AccountService accountService,
                       AppEventService eventService,
                       @Value("${app.newsletter.schedule}") String cron) {
        this.propertyService = propertyService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.msgSrv = msgSrv;
        this.dbSource = dataBasePropertySource;
        this.accountService = accountService;
        this.eventService = eventService;
        avatarBuffer = new HashMap<>();
        this.cron_scheduler = cron;
        initMailSender();
        schedulerLookup();
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

    private void schedulerLookup() {
        org.springframework.scheduling.support.CronTrigger trigger =
                new CronTrigger(cron_scheduler);
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
        String message = "Next scheduled email sending is : " + Utils.convertDateTimeToString(nextExecutionTime);
        LOG.info(message);
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
    public void sendEmail(Mail mail) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String from = propertyService.getProperty(APP_EMAIL_FROM);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(mail.getMailSubject());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(mail.getMailTo());
            mail.setMailContent(geContentFromTemplate(mail.getModel(), "emailTemplate.ftl"));
            mimeMessageHelper.setText(mail.getMailContent(), true);
            mailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
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
    public void shareGiftList(List<Mail> emails) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        String publicLink = application + "#/public/" + Utils.getCurrentAccountId();
        for (Mail mail : emails) {
            Locale locale = getMailLocale(mail);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
            mimeMessageHelper.setSubject(msgSrv.getMessage("gift.share.subject", new Object[]{Utils.getCurrentAccount().getFullname()}, "", locale));
            mimeMessageHelper.setFrom(from);
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
    public void notifyAboutGiftRemoved(Gift gift) throws MessagingException, AccountNotFoundException {
        Account owner = accountService.findById(gift.getUserId());
        Mail mail = Utils.createMail(gift.getClaimed(), owner);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        String listLink = application + "#/list/" + Utils.getCurrentAccountId();
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("gift.delete.notify", new Object[]{owner.getFullname()}, "", locale));
        mimeMessageHelper.setFrom(from);
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
            LOG.error("Failed to properly resize image. {}", e);
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
    public void sendConfirmMail(Mail mail, AccountEvent event) throws MessagingException {
        MimeMessageHelper mimeMessageHelper = createBaseMimeMessage(mail);
        Locale locale = getMailLocale(mail);
        String confirmLink = mail.getModel().get(APPLICATION) + "#/confirm/" + event.getToken();
        mail.addToModel(CONFIRM_LINK, confirmLink);
        String familyName = event.getGroup().getName();
        switch (event.getType()) {
            case GROUP_MEMEBER:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.group.invite", new Object[]{familyName}, "", locale));
                mail.addToModel(GROUP_NAME, familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/groupInvite.ftl"));
                break;
            case GROUP_ADMIN:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.group.admin", new Object[]{familyName}, "", locale));
                mail.addToModel(GROUP_NAME, familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/groupAdmin.ftl"));
                break;
            case GROUP_REMOVE:
                break;
        }
        mimeMessageHelper.setText(mail.getMailContent(), true);
        File avatarTempFile = getUserAvatar(Utils.getCurrentAccount());
        mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
        addAppLogo(mimeMessageHelper);
        LOG.info("Sending confirm message to {}", mail.getMailTo());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    private MimeMessageHelper createBaseMimeMessage(Mail mail) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        return mimeMessageHelper;
    }

    public void sendInvite(Mail mail, String familyName) throws MessagingException {
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
    public void sendEvents() throws MessagingException {
        LOG.info("Begin sending scheduled newsletter");
        int mailsSent = 0;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        List<Account> allAccounts = accountService.findAllWithNotifications();//TODO has newsletter checked
        Map<Account, List<AppEvent>> eventMap = eventService.getEventsGroupedByAccount();
        for (Account account : allAccounts) {
            Map<Account, List<AppEvent>> eventsWithoutAccount = eventMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(account))
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
            if (!eventsWithoutAccount.isEmpty()) {
                sendEventForAccount(mimeMessage, application, eventsWithoutAccount, account);
                mailsSent++;
            }
        }
        eventService.processEvents(eventService.findAllNotProcessed());
        LOG.info("Newsletter sent to {} recipients", mailsSent);
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
    private void sendEventForAccount(MimeMessage mimeMessage, String application, Map<Account, List<AppEvent>> eventMap, Account account) throws MessagingException {
        Mail mail = Utils.createMail(account);
        String from = propertyService.getProperty(APP_EMAIL_FROM);
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("schedule.event.summary", null, "", locale));
        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        mail.addToModel(NAME, account.getName());
        mail.addToModel(EVENTS, eventMap);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/scheduler.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        addAppLogo(mimeMessageHelper);
        addEventAccountsAvatars(eventMap, mimeMessageHelper);
        LOG.debug("Sending scheduled email to {}", account.getEmail());
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    private void addEventAccountsAvatars(Map<Account, List<AppEvent>> eventMap, MimeMessageHelper mimeMessageHelper) throws MessagingException {
        for (Account eventAccount : eventMap.keySet()) {
            mimeMessageHelper.addInline(AVATAR + eventAccount.getId(), getUserAvatar(eventAccount));
        }
    }

    private void addAppLogo(MimeMessageHelper mimeMessageHelper) throws MessagingException {
        mimeMessageHelper.addInline(LOGO_PNG, new ClassPathResource("static/images/logo_email.png"));
    }
}
