package com.qprogramming.gifts.config.mail;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.config.property.DataBasePropertySource;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEvent;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.schedule.AppEventType;
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

import static com.qprogramming.gifts.TestUtil.USER_RANDOM_ID;
import static com.qprogramming.gifts.settings.Settings.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        when(propertyServiceMock.getProperty(APP_EMAIL_FROM)).thenReturn("from@mail.com");
        Mail mail = new Mail();
        mail.setMailTo("to@mail.com");
        mailService.shareGiftList(Collections.singletonList(mail));
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendMemberConfirmMail() throws MessagingException {
        AccountEvent event = new AccountEvent();
        Group group = new Group();
        group.setName(testAccount.getSurname());
        event.setGroup(group);
        event.setToken("token");
        event.setType(AccountEventType.GROUP_MEMEBER);
        when(msgSrvMock.getMessage("user.group.invite", new Object[]{group.getName()}, "", locale)).thenReturn(SUBJECT);
        when(propertyServiceMock.getProperty(APP_EMAIL_FROM)).thenReturn("from@mail.com");
        Mail mail = Utils.createMail(testAccount);
        mailService.sendConfirmMail(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendAdminConfirmMail() throws MessagingException {
        AccountEvent event = new AccountEvent();
        Group group = new Group();
        group.setName(testAccount.getSurname());
        event.setGroup(group);
        event.setToken("token");
        event.setType(AccountEventType.GROUP_ADMIN);
        when(msgSrvMock.getMessage("user.group.admin", new Object[]{group.getName()}, "", locale)).thenReturn(SUBJECT);
        when(propertyServiceMock.getProperty(APP_EMAIL_FROM)).thenReturn("from@mail.com");
        Mail mail = Utils.createMail(testAccount);
        mailService.sendConfirmMail(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEvents() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setNotifications(true);
        Group group = new Group();
        group.addMember(testAccount);
        group.addMember(account);
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        AppEvent event3 = TestUtil.createEvent(testAccount);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(event1, event2, event3));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEventsWithoutRecipient() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setNotifications(true);
        Account account2 = TestUtil.createAccount("Johny", "Doe");
        account2.setNotifications(true);
        account2.setId(USER_RANDOM_ID + 1);
        Group group = new Group();
        group.addMember(testAccount);
        group.addMember(account);
        group.addMember(account2);
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        AppEvent event3 = TestUtil.createEvent(testAccount);
        AppEvent event4 = TestUtil.createEvent(testAccount);
        event4.setCreatedBy(account2);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(event1, event2, event3, event4));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(2)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEventsHiddenGiftWithoutRecipient() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setNotifications(true);
        Group group = new Group();
        group.addMember(testAccount);
        group.addMember(account);
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        event1.setCreatedBy(account);
        event2.setCreatedBy(account);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(event1, event2));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(0)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEventsForNewAndRealised() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setNotifications(true);
        Group group = new Group();
        group.addMember(testAccount);
        group.addMember(account);
        AppEvent new_event1 = TestUtil.createEvent(testAccount);
        AppEvent new_event2 = TestUtil.createEvent(testAccount);
        AppEvent realised_event1 = TestUtil.createEvent(testAccount);
        new_event1.setType(AppEventType.NEW);
        new_event2.getGift().setId(2L);
        new_event2.setType(AppEventType.NEW);
        realised_event1.setGift(new_event1.getGift());
        realised_event1.setType(AppEventType.REALISED);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(new_event1, new_event2, realised_event1));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void sendEventsForDifferentGroupsTest() throws MessagingException {
        Account account = TestUtil.createAccount("John", "Doe");
        account.setNotifications(true);
        Account account2 = TestUtil.createAccount("Johny", "Doe");
        account2.setNotifications(true);
        account2.setId(USER_RANDOM_ID + 1);
        Account account3 = TestUtil.createAccount("Johny", "Smith");
        account3.setNotifications(true);
        account3.setId(USER_RANDOM_ID + 2);
        Account account4 = TestUtil.createAccount("Mark", "Smith");
        account4.setNotifications(true);
        account4.setId(USER_RANDOM_ID + 3);
        Group group = new Group();
        group.setId(1L);
        group.addMember(testAccount);
        group.addMember(account);
        group.addMember(account2);
        Group group2 = new Group();
        group.setId(2L);
        group2.addMember(account3);
        group2.addMember(account4);
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        AppEvent event3 = TestUtil.createEvent(testAccount);
        AppEvent event4 = TestUtil.createEvent(testAccount);
        AppEvent event5 = TestUtil.createEvent(account3);
        AppEvent event6 = TestUtil.createEvent(account3);
        event4.setCreatedBy(account2);
        Map<Account, List<AppEvent>> events = new HashMap<>();
        events.put(testAccount, Arrays.asList(event1, event2, event3, event4));
        events.put(account3, Arrays.asList(event5, event6));
        when(eventServiceMock.getEventsGroupedByAccount()).thenReturn(events);
        when(msgSrvMock.getMessage("schedule.event.summary", null, "", locale)).thenReturn(SUBJECT);
        mailService.sendEvents();
        verify(mailSenderMock, times(3)).send(any(MimeMessage.class));
    }


}
