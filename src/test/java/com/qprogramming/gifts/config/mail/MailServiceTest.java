package com.qprogramming.gifts.config.mail;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEvent;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.support.Utils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static com.qprogramming.gifts.settings.Settings.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class MailServiceTest {

    private static final String SUBJECT = "Subject";
    private static final String MAIL_FROM_COM = "mail@from.com";
    private static final String UTF_8 = "UTF-8";
    private static final String URL = "url";
    private static final String EN = "en";
    private static final String CRON = "0 0 10-12 * * MON";
    private MailService mailService;
    private Account testAccount;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private JavaMailSenderImpl mailSenderMock;
    @Mock
    private Configuration freemarkerConfigurationMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private DataBasePropertySource dbSourceMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private AppEventService eventServiceMock;
    @Mock
    private Template templateMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    private Locale locale;
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());


    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        locale = new Locale("en");
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_ENCODING)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_DEFAULT_LANG)).thenReturn(EN);
        when(propertyServiceMock.getProperty(APP_EMAIL_FROM)).thenReturn(MAIL_FROM_COM);
        when(freemarkerConfigurationMock.getTemplate(anyString())).thenReturn(templateMock);
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        SecurityContextHolder.setContext(securityMock);
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, dbSourceMock, accountServiceMock, eventServiceMock, CRON) {
            @Override
            public void initMailSender() {
                this.mailSender = mailSenderMock;
            }
        };
    }

    @Test
    public void testInitWithBadPort() {
        when(propertyServiceMock.getProperty(APP_EMAIL_HOST)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_PORT)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_EMAIL_USERNAME)).thenReturn("user");
        when(propertyServiceMock.getProperty(APP_EMAIL_PASS)).thenReturn("pass");
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, dbSourceMock, accountServiceMock, eventServiceMock, CRON);
    }

    @Test
    public void testConnection() {
        assertTrue(mailService.testConnection());
    }

    @Test
    public void testConnectionWithException() throws MessagingException {
        doThrow(new MessagingException()).when(mailSenderMock).testConnection();
        assertFalse(mailService.testConnection());
    }

    @Test(expected = MessagingException.class)
    public void testConnectionWithCredentialsThrowsException() throws MessagingException {
        mailService.testConnection("", 25, "", "");
    }

    @Test
    public void shareGiftList() throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        Locale locale = new Locale("en");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage("gift.share.subject", new Object[]{testAccount.getFullname()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = new Mail();
        mail.setMailFrom("from@mail.com");
        mail.setMailTo("to@mail.com");
        mailService.shareGiftList(Collections.singletonList(mail));
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendMemberConfirmMail() throws MessagingException {
        AccountEvent event = new AccountEvent();
        Family family = new Family();
        family.setName(testAccount.getSurname());
        event.setFamily(family);
        event.setToken("token");
        event.setType(AccountEventType.FAMILY_MEMEBER);
        when(msgSrvMock.getMessage("user.family.invite", new Object[]{family.getName()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = Utils.createMail(testAccount);
        mail.setMailFrom(MAIL_FROM_COM);
        mailService.sendConfirmMail(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendAdminConfirmMail() throws MessagingException {
        AccountEvent event = new AccountEvent();
        Family family = new Family();
        family.setName(testAccount.getSurname());
        event.setFamily(family);
        event.setToken("token");
        event.setType(AccountEventType.FAMILY_ADMIN);
        when(msgSrvMock.getMessage("user.family.admin", new Object[]{family.getName()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = Utils.createMail(testAccount);
        mail.setMailFrom(MAIL_FROM_COM);
        mailService.sendConfirmMail(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEvents() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        AppEvent event3 = TestUtil.createEvent(testAccount);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(event1, event2, event3));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(accountServiceMock.findAllWithNewsletter()).thenReturn(Arrays.asList(testAccount, account));
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }
}
