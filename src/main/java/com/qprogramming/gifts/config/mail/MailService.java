package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.Utils;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static com.qprogramming.gifts.settings.Settings.*;

@Service
public class MailService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private JavaMailSender mailSender;
    private Configuration freemarkerConfiguration;
    private MessagesService msgSrv;


    @Autowired
    public MailService(PropertyService propertyService, @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration, MessagesService msgSrv) {
        this.propertyService = propertyService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.msgSrv = msgSrv;
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

    public void shareGiftList(Map<Category, List<Gift>> gifts, List<Mail> emails) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        for (Mail mail : emails) {
            Locale locale = getMailLocale(mail);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(msgSrv.getMessage("gift.share.subject", new Object[]{Utils.getCurrentAccount().getFullname()}, "", locale));
            mimeMessageHelper.setFrom(mail.getMailFrom());
            mimeMessageHelper.setTo(mail.getMailTo());
            mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/giftList.ftl"));
            mimeMessageHelper.setText(mail.getMailContent(), true);
            mailSender.send(mimeMessageHelper.getMimeMessage());
        }
    }

    private Locale getMailLocale(Mail mail) {
        return mail.getLocale() != null ? new Locale(mail.getLocale()) : new Locale(propertyService.getProperty(APP_DEFAULT_LANG));
    }


}
