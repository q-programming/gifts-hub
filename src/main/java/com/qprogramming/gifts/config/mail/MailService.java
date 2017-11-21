package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
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
    private static final String FAMILY_NAME = "familyName";
    private static final String CONFIRM_LINK = "confirmLink";
    private static final String LOGO_PNG = "logo.png";
    private static final String AVATAR = "avatar_";
    private static final String USER_AVATAR_PNG = "userAvatar.png";
    private static final String PNG = "png";
    private static final String PUBLIC_LINK = "publicLink";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    JavaMailSender mailSender;
    private Configuration freemarkerConfiguration;
    private MessagesService msgSrv;
    private DataBasePropertySource dbSource;
    private AccountService accountService;
    private AppEventService eventService;
    private Map<Account, File> avatarBuffer;


    @Autowired
    public MailService(PropertyService propertyService, @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration, MessagesService msgSrv, DataBasePropertySource dataBasePropertySource, AccountService accountService, AppEventService eventService) {
        this.propertyService = propertyService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.msgSrv = msgSrv;
        this.dbSource = dataBasePropertySource;
        this.accountService = accountService;
        this.eventService = eventService;
        avatarBuffer = new HashMap<>();
        initMailSender();
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
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(mail.getMailSubject());
            mimeMessageHelper.setFrom(mail.getMailFrom());
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
        String publicLink = application + "#/public/" + Utils.getCurrentAccountId();
        for (Mail mail : emails) {
            Locale locale = getMailLocale(mail);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
            mimeMessageHelper.setSubject(msgSrv.getMessage("gift.share.subject", new Object[]{Utils.getCurrentAccount().getFullname()}, "", locale));
            mimeMessageHelper.setFrom(mail.getMailFrom());
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
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String confirmLink = application + "#/confirm/" + event.getToken();
        mail.addToModel(CONFIRM_LINK, confirmLink);
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        String familyName = event.getFamily().getName();
        mimeMessageHelper.setFrom(mail.getMailFrom());
        mimeMessageHelper.setTo(mail.getMailTo());
        addAppLogo(mimeMessageHelper);
        File avatarTempFile = getUserAvatar(Utils.getCurrentAccount());
        mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
        mail.addToModel(APPLICATION, application);
        switch (event.getType()) {
            case FAMILY_MEMEBER:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.family.invite", new Object[]{familyName}, "", locale));
                mail.addToModel(FAMILY_NAME, familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/familyInvite.ftl"));
                break;
            case FAMILY_ADMIN:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.family.admin", new Object[]{familyName}, "", locale));
                mail.addToModel(FAMILY_NAME, familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/familyAdmin.ftl"));
                break;
            case FAMILY_REMOVE:
                break;
        }
        mimeMessageHelper.setText(mail.getMailContent(), true);
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }

    /**
     * Send all scheduled events. While sending email to account it's events are omitted
     * All events are purged afterwards ( marked as processed )
     *
     * @throws MessagingException if there were errors while sending email
     */
    public void sendEvents() throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        List<Account> allAccounts = accountService.findAllWithNewsletter();//TODO has newsletter checked
        Map<Account, List<AppEvent>> eventMap = eventService.getEventsGroupedByAccount();
        for (Account account : allAccounts) {
            Map<Account, List<AppEvent>> eventsWithoutAccount = eventMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(account))
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
            if (!eventsWithoutAccount.isEmpty()) {
                sendEventForAccount(mimeMessage, application, eventsWithoutAccount, account);
            }
        }
    }

    @Deprecated
    //TODO remove after complete
    public void sendEvent() throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        Map<Account, List<AppEvent>> eventMap = eventService.getEventsGroupedByAccount();
        sendEventForAccount(mimeMessage, application, eventMap, Utils.getCurrentAccount());
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
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, propertyService.getProperty(Settings.APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(msgSrv.getMessage("schedule.event.summary", null, "", locale));
        mimeMessageHelper.setFrom(mail.getMailFrom());
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
