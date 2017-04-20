package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.GiftStatus;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryRepository;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.Utils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GiftRestControllerTest {
    public static final String API_GIFT_CREATE = "/api/gift/create";
    public static final String API_GIFT_EDIT = "/api/gift/edit";
    public static final String API_GIFT_LIST = "/api/gift/user";
    public static final String API_GIFT_CLAIM = "/api/gift/claim/";
    public static final String API_GIFT_UNCLAIM = "/api/gift/unclaim/";
    public static final String API_GIFT_COMPLETE = "/api/gift/complete/";
    public static final String API_GIFT_UNDO_COMPLETE = "/api/gift/undo-complete/";
    public static final String API_GIFT_DELETE = "/api/gift/delete/";
    public static final String OTHER_USER = "OTHER_USER";
    public static final String JOHN = "John";
    public static final String DOE = "Doe";
    public static final String NAME = "name";
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
    @Mock
    private MessagesService messagesServiceMock;
    @Mock
    private FamilyService familyServiceMock;
    @Mock
    private AnonymousAuthenticationToken annonymousTokenMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        GiftRestController giftsCtrl = new GiftRestController(accSrvMock, giftServiceMock, searchEngineServiceMock, categoryRepositoryMock, messagesServiceMock, familyServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.giftsRestCtrl = MockMvcBuilders.standaloneSetup(giftsCtrl).build();
    }

    @Test
    public void createGiftSuccess() throws Exception {
        GiftForm form = new GiftForm();
        form.setName("Gift");
        form.setDescription("Some sample description");
        form.setLink("http://google.com");
        List<Long> idList = Arrays.asList(1L, 2L);
        form.setSearchEngines(idList);
        form.setCategory("cat2");
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
    public void editGiftSuccess() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("Name");
        gift.setUserId(testAccount.getId());
        GiftForm form = new GiftForm();
        form.setId(1L);
        form.setName("Gift");
        form.setDescription("Some sample new description");
        form.setLink("http://google.com");
        List<Long> idList = Collections.singletonList(1L);
        form.setSearchEngines(idList);
        form.setCategory("cat2");
        Set<SearchEngine> engines = new HashSet<>();
        engines.add(TestUtil.createSearchEngine(NAME, "link", "icon"));
        when(giftServiceMock.findById(1L)).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        when(searchEngineServiceMock.getSearchEngines(form.getSearchEngines()))
                .thenReturn(engines);
        when(categoryRepositoryMock.save(any(Category.class))).then(returnsFirstArg());
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        MvcResult mvcResult = giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Gift result = TestUtil.convertJsonToObject(contentAsString, Gift.class);
        verify(giftServiceMock, times(1)).update(any(Gift.class));
        verify(categoryRepositoryMock, times(1)).save(any(Category.class));
        assertEquals(form.getName(), result.getName());
        assertEquals(form.getDescription(), result.getDescription());
        assertTrue(result.getEngines().size() == 1);

    }

    @Test
    public void editGiftNotFound() throws Exception {
        GiftForm form = new GiftForm();
        form.setId(1L);
        giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void editGiftNotOwner() throws Exception {
        GiftForm form = new GiftForm();
        form.setId(1L);
        form.setName("New Name");
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("Name");
        gift.setUserId("other");
        when(giftServiceMock.findById(1L)).thenReturn(gift);
        giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
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
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        List<Gift> giftList = Collections.singletonList(gift);
        Map<Category, List<Gift>> expected = Utils.toGiftTreeMap(giftList);
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(testAccount);
        when(giftServiceMock.findAllByUser(testAccount.getId())).thenReturn(expected);
        when(giftServiceMock.toGiftTreeMap(anyListOf(Gift.class), anyBoolean())).thenCallRealMethod();
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(testAccount.getId()));
        assertTrue(contentAsString.contains(NAME));
    }

    @Test
    public void getGiftsAccountPublic() throws Exception {
        testAccount.setPublicList(true);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        List<Gift> giftList = Collections.singletonList(gift);
        Map<Category, List<Gift>> expected = Utils.toGiftTreeMap(giftList);
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(testAccount);
        when(giftServiceMock.findAllByUser(testAccount.getId())).thenReturn(expected);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(testAccount.getId()));
        assertTrue(contentAsString.contains(NAME));
    }

    @Test
    public void getGiftsAccountNotPublic() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        List<Gift> giftList = Collections.singletonList(gift);
        Map<Category, List<Gift>> expected = Utils.toGiftTreeMap(giftList);
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(testAccount);
        when(giftServiceMock.findAllByUser(testAccount.getId())).thenReturn(expected);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getId())).andExpect(status().isBadRequest());
    }

    @Test
    public void claimGift() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId("2");
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_CLAIM + gift.getId()))
                .andExpect(status().isOk());
        gift.setClaimed(testAccount);
        verify(giftServiceMock, times(1)).update(gift);
    }

    @Test
    public void unClaimGift() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId("2");
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        gift.setClaimed(testAccount);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_UNCLAIM + gift.getId()))
                .andExpect(status().isOk());
        gift.setClaimed(null);
        verify(giftServiceMock, times(1)).update(gift);
    }

    @Test
    public void unClaimGiftNotClaimed() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId("2");
        Account claimedBy = TestUtil.createAccount(JOHN, DOE);
        owner.setId("3");
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        gift.setClaimed(claimedBy);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_UNCLAIM + gift.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void claimGiftSameUser() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        JSONObject object = new JSONObject();
        object.put("id", 1L);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_CLAIM + gift.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void completeGift() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_COMPLETE + gift.getId()))
                .andExpect(status().isOk());
        verify(giftServiceMock, times(1)).update(any(Gift.class));
        assertTrue(GiftStatus.REALISED.equals(gift.getStatus()));
    }

    @Test
    public void completeGiftWrongUser() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId("OTHER");
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        giftsRestCtrl.perform(
                put(API_GIFT_COMPLETE + gift.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void undoCompleteGift() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        gift.setStatus(GiftStatus.REALISED);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        giftsRestCtrl.perform(
                put(API_GIFT_UNDO_COMPLETE + gift.getId()))
                .andExpect(status().isOk());
        verify(giftServiceMock, times(1)).update(any(Gift.class));
        assertNull(gift.getStatus());
    }

    @Test
    public void undoCompleteGiftWrongUser() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(OTHER_USER);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        giftsRestCtrl.perform(
                put(API_GIFT_UNDO_COMPLETE + gift.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteGiftNotExisting() throws Exception {
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + "/2")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteGiftWrongUser() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(OTHER_USER);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + "/1")).andExpect(status().isBadRequest());
    }

    @Test
    public void deleteOwnGiftSuccess() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
    }

    @Test
    public void deleteGiftAsFamilyAdminSuccess() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId(OTHER_USER);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        Family family = new Family();
        family.getAdmins().add(testAccount);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accSrvMock.findById(owner.getId())).thenReturn(owner);
        when(familyServiceMock.getFamily(owner)).thenReturn(family);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
    }


}
