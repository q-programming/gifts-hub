package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryRepository;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Mock
    private SearchEngineService searchEngineServiceMock;
    @Mock
    private CategoryRepository categoryRepositoryMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        GiftRestController giftsCtrl = new GiftRestController(accSrvMock, giftServiceMock, searchEngineServiceMock, categoryRepositoryMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.giftsRestCtrl = MockMvcBuilders.standaloneSetup(giftsCtrl).build();
    }

    @Test
    public void createGiftSuccess() throws Exception {
        GiftForm form = new GiftForm();
        form.setName("GIft");
        form.setDescription("Some sample description");
        form.setLink("http://google.com");
        List<Long> idList = new ArrayList<>();
        idList.add(1L);
        idList.add(2L);
        Category cat2 = new Category("cat2");
        form.setSearchEngines(idList);
        form.setCategory("cat2");
        when(categoryRepositoryMock.findByName(cat2.getName())).thenReturn(cat2);
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isCreated());
        verify(giftServiceMock, times(1)).create(any(Gift.class));
        verify(categoryRepositoryMock, times(1)).save(any(Category.class));
    }

    @Test
    public void createGiftEmptyName() throws Exception {
        GiftForm form = new GiftForm();
        form.setLink("http://google.com");
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getGiiftsAccountNotFound() throws Exception {
        giftsRestCtrl.perform(post(API_GIFT_LIST + "/notExisting"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void getGiftsAccount() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("name");
        gift.setUserId(testAccount.getId());
        List<Gift> giftList = new ArrayList<>();
        giftList.add(gift);
        Map<Category, List<Gift>> expected = Utils.toGiftTreeMap(giftList);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        when(giftServiceMock.findAllByUser(testAccount.getId())).thenReturn(expected);
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getUsername())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(testAccount.getId()));
        assertTrue(contentAsString.contains("name"));
    }


}
