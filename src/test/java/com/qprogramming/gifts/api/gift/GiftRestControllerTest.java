package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 10.03.2017.
 */
public class GiftRestControllerTest {
    public static final String API_GIFT_CREATE = "/api/gift/create";
    public static final String API_GIFT_LIST = "/api/gift/user";
    private MockMvc giftsRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private GiftService giftServiceMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        GiftRestController giftsCtrl = new GiftRestController(accSrvMock, giftServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.giftsRestCtrl = MockMvcBuilders.standaloneSetup(giftsCtrl).build();
    }

    @Test
    public void createGiftSuccess() throws Exception {
        JSONObject object = new JSONObject();
        object.put(Gift.NAME, "Gift");
        object.put(Gift.DESCRIPTION, "Some sample description");
        object.put(Gift.CATEGORY, "Some Category");
        object.put(Gift.LINK, "http://google.com");
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(object.toString()))
                .andExpect(status().isCreated());
        verify(giftServiceMock, times(1)).create(any(Gift.class));
    }

    @Test
    public void createGiftEmptyName() throws Exception {
        JSONObject object = new JSONObject();
        object.put(Gift.CATEGORY, "new");
        object.put(Gift.LINK, "http://google.com");
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(object.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getGiiftsAccountNotFound() throws Exception {
        giftsRestCtrl.perform(post(API_GIFT_LIST + "notExisting"))
                .andExpect(status().isNotFound());
    }


}
