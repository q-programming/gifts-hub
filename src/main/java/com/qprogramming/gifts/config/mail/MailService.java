package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.messages.MessagesService;
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
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.qprogramming.gifts.settings.Settings.*;

@Service
public class MailService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private JavaMailSender mailSender;
    private Configuration freemarkerConfiguration;
    private MessagesService msgSrv;
    private DataBasePropertySource dbSource;
    private AccountService accountService;


    @Autowired
    public MailService(PropertyService propertyService, @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration, MessagesService msgSrv, DataBasePropertySource dataBasePropertySource, AccountService accountService) {
        this.propertyService = propertyService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.msgSrv = msgSrv;
        this.dbSource = dataBasePropertySource;
        this.accountService = accountService;
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
     * @param host
     * @param port
     * @param username
     * @param password
     * @throws MessagingException
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

    public String geContentFromTemplate(Map<String, Object> model, String emailTemplate) {
        StringBuffer content = new StringBuffer();
        try {
            content.append(FreeMarkerTemplateUtils
                    .processTemplateIntoString(freemarkerConfiguration.getTemplate(emailTemplate), model));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

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
            mail.addToModel("publicLink", publicLink);
            mail.addToModel("application", application);
            mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/giftList.ftl"));
            mimeMessageHelper.setText(mail.getMailContent(), true);
            mimeMessageHelper.addInline("logo.png", new ClassPathResource("static/images/logo_email.png"));
            addUserAvatar(mimeMessageHelper, Utils.getCurrentAccount());
            mailSender.send(mimeMessageHelper.getMimeMessage());
        }
    }

    private void addUserAvatar(MimeMessageHelper mimeMessageHelper, Account account) throws MessagingException {
        try {
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
            File temp = File.createTempFile(account.getId(), ".png");
            ImageIO.write(scaledImg, "png", temp);
            mimeMessageHelper.addInline("userAvatar.png", temp);
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Locale getMailLocale(Mail mail) {
        return mail.getLocale() != null ? new Locale(mail.getLocale()) : new Locale(propertyService.getProperty(APP_DEFAULT_LANG));
    }


    public void sendConfirmMail(Mail mail, AccountEvent event) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String application = propertyService.getProperty(APP_URL);
        String confirmLink = application + "#/confirm/" + event.getToken();
        mail.addToModel("confirmLink", confirmLink);
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        String familyName = event.getFamily().getName();
        mimeMessageHelper.setFrom(mail.getMailFrom());
        mimeMessageHelper.setTo(mail.getMailTo());
        mimeMessageHelper.addInline("logo.png", new ClassPathResource("static/images/logo_email.png"));
        addUserAvatar(mimeMessageHelper, Utils.getCurrentAccount());
        mail.addToModel("application", application);
        switch (event.getType()) {
            case FAMILY_MEMEBER:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.family.invite", new Object[]{familyName}, "", locale));
                mail.addToModel("familyName", familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/familyInvite.ftl"));
                break;
            case FAMILY_ADMIN:
                mimeMessageHelper.setSubject(msgSrv.getMessage("user.family.admin", new Object[]{familyName}, "", locale));
                mail.addToModel("familyName", familyName);
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/familyAdmin.ftl"));
                break;
            case FAMILY_REMOVE:
                break;
        }
        mimeMessageHelper.setText(mail.getMailContent(), true);
        mailSender.send(mimeMessageHelper.getMimeMessage());

    }
}
