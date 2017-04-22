package com.qprogramming.gifts.config.mail;


import com.qprogramming.gifts.config.property.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Properties;

import static com.qprogramming.gifts.settings.Settings.*;

@Service
public class MailService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PropertyService propertyService;
    private MailSender mailSender;

    @Autowired
    public MailService(PropertyService propertyService) {
        this.propertyService = propertyService;
        initMailSender();
    }

    private void initMailSender() {
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
        javaMailProperties.setProperty("mail.smtp.auth", propertyService.getProperty(APP_EMAIL_SMTP_AUTH));
        javaMailProperties.setProperty("mail.smtp.starttls.enable", propertyService.getProperty(APP_EMAIL_START_TTLS));
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
     * @param auth
     * @param tls
     * @throws MessagingException
     */
    public void testConnection(String host, Integer port, String username, String password, boolean auth, boolean tls) throws MessagingException {
        JavaMailSenderImpl jmsi = new JavaMailSenderImpl();
        jmsi.setHost(host);
        jmsi.setPort(port);
        jmsi.setUsername(username);
        jmsi.setPassword(password);
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", String.valueOf(auth));
        javaMailProperties.setProperty("mail.smtp.starttls.enable", String.valueOf(tls));
        jmsi.setJavaMailProperties(javaMailProperties);
        jmsi.testConnection();
    }
}
