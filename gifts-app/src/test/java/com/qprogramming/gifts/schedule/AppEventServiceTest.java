package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.support.Utils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.qprogramming.gifts.TestUtil.createEvent;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AppEventServiceTest {

    private AppEventService eventSrv;
    private Account testAccount;

    @Mock
    private AppEventRepo eventRepoMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    @Mock
    private AccountService accountServiceMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventSrv = new AppEventService(eventRepoMock, accountServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
    }

    @Test
    public void getNotProcessedTest() {
        when(eventRepoMock.findAll()).thenReturn(Collections.singletonList(createEvent(testAccount)));
        List<AppEvent> result = eventSrv.findAllNotProcessed();
        assertFalse(result.isEmpty());
        assertEquals(testAccount, result.get(0).getAccount());
    }

    @Test
    public void markProcessedTest() {
        AppEvent event = createEvent(testAccount);
        AppEvent event2 = createEvent(testAccount);
        List<AppEvent> appEvents = Arrays.asList(event, event2);
        when(eventRepoMock.findAll()).thenReturn(appEvents);
        eventSrv.processEvents();
        verify(eventRepoMock, times(1)).deleteAll(anyCollection());
    }

    @Test
    public void getAllNotProcessedMap() {
        AppEvent event = createEvent(testAccount);
        AppEvent event2 = createEvent(testAccount);
        List<AppEvent> appEvents = Arrays.asList(event, event2);
        when(eventRepoMock.findAll()).thenReturn(appEvents);
        Map<Account, List<AppEvent>> result = eventSrv.getEventsGroupedByAccount();
        assertTrue(result.get(testAccount).size() > 1);
    }

    @Test
    public void addEventTest() throws AccountNotFoundException {
        Gift gift = new Gift();
        gift.setName("name");
        gift.setUserId(testAccount.getId());
        eventSrv.addEvent(gift, AppEventType.NEW);
        eventSrv.addEvent(gift, AppEventType.REALISED);
        eventSrv.addEvent(gift, AppEventType.DELETED);
        verify(eventRepoMock, times(3)).save(any(AppEvent.class));

    }

    @Test
    public void tryToUndoTest() throws AccountNotFoundException {
        AppEvent event = createEvent(testAccount);
        when(eventRepoMock.findByAccountAndGiftAndType(Utils.getCurrentAccount(), event.getGift(), AppEventType.REALISED)).thenReturn(event);
        eventSrv.tryToUndoEvent(event.getGift());
        verify(eventRepoMock, times(1)).findByAccountAndGiftAndType(testAccount, event.getGift(), AppEventType.REALISED);
    }

    @Test
    public void getEventsGroupedByAccountTest() {
        AppEvent event1 = TestUtil.createEvent(testAccount);
        AppEvent event2 = TestUtil.createEvent(testAccount);
        AppEvent event3 = TestUtil.createEvent(testAccount);
        AppEvent event4 = TestUtil.createEvent(testAccount);
        event4.setCreatedBy(testAccount);
        when(eventRepoMock.findAll()).thenReturn(Arrays.asList(event1, event2, event3, event4));
        Map<Account, List<AppEvent>> eventsGroupedByAccount = eventSrv.getEventsGroupedByAccount();
        assertTrue(eventsGroupedByAccount.containsKey(testAccount));
        assertEquals(3, eventsGroupedByAccount.get(testAccount).size());
    }

}