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


    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        locale = new Locale("en");
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn("url");
        when(propertyServiceMock.getProperty(APP_EMAIL_ENCODING)).thenReturn("UTF-8");
        when(propertyServiceMock.getProperty(APP_DEFAULT_LANG)).thenReturn("en");
        when(freemarkerConfigurationMock.getTemplate(anyString())).thenReturn(templateMock);
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));

        SecurityContextHolder.setContext(securityMock);
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, dbSourceMock, accountServiceMock, eventServiceMock) {
            @Override
            public void initMailSender() {
                this.mailSender = mailSenderMock;
            }
        };
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
