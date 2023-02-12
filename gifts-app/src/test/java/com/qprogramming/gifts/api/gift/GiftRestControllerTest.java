package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.GiftStatus;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryService;
import com.qprogramming.gifts.gift.image.GiftImage;
import com.qprogramming.gifts.security.AnonAuthentication;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.schedule.AppEventType;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.Utils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.TestUtil.createCategory;
import static com.qprogramming.gifts.TestUtil.createSearchEngine;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GiftRestControllerTest extends MockedAccountTestBase {
    private static final String API_GIFT_CREATE = "/api/gift/create";
    private static final String API_GIFT_EDIT = "/api/gift/edit";
    private static final String API_GIFT_LIST = "/api/gift/user";
    private static final String API_GIFT_CLAIMED = "/api/gift/claimed";
    private static final String API_GIFT_CLAIM = "/api/gift/claim/";
    private static final String API_GIFT_UNCLAIM = "/api/gift/unclaim/";
    private static final String API_GIFT_COMPLETE = "/api/gift/complete/";
    private static final String API_GIFT_UNDO_COMPLETE = "/api/gift/undo-complete/";
    private static final String API_GIFT_DELETE = "/api/gift/delete/";
    private static final String API_GIFT_IMPORT = "/api/gift/import";
    private static final String API_GIFT_TEMPLATE = "/api/gift/get-template";
    private static final String API_GIFT_ALLOWED_CATEGORY = "/api/gift/allowed-category";
    private static final String API_GIFT_PICTURE = "/api/gift/image/";
    private static final String OTHER_USER = "OTHER_USER";
    private static final String JOHN = "John";
    private static final String DOE = "Doe";
    private static final String NAME = "name";
    private static final String TEMPLATE_XLS = "template.xls";
    public static final String NEW_URL = "newUrl";
    private static final String URL = "http://google.com";
    private static final String GIFT_NAME = "Gift";
    private static final String GIFT_DESCRIPTION = "Some sample description";
    private MockMvc giftsRestCtrl;
    @Mock
    private AccountService accSrvMock;
    @Mock
    private GiftService giftServiceMock;
    @Mock
    private SearchEngineService searchEngineServiceMock;
    @Mock
    private CategoryService categoryServiceMock;
    @Mock
    private MessagesService messagesServiceMock;
    @Mock
    private GroupService groupServiceMock;
    @Mock
    private AnonAuthentication annonymousTokenMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private ServletOutputStream outputStreamMock;
    @Mock
    private MockMultipartFile mockMultipartFile;
    @Mock
    private AppEventService eventServiceMock;
    @Mock
    private MailService mailServiceMock;

    @Before
    public void setUp() throws Exception {
        super.setup();
        when(messagesServiceMock.getMessage("gift.category.other", null, "", new Locale("en"))).thenReturn("Other");
        GiftRestController giftsCtrl = new GiftRestController(accSrvMock, giftServiceMock, searchEngineServiceMock, categoryServiceMock, messagesServiceMock, groupServiceMock, eventServiceMock, mailServiceMock);
        this.giftsRestCtrl = MockMvcBuilders.standaloneSetup(giftsCtrl).build();
    }

    @Test
    public void createGiftSuccess() throws Exception {
        Category cat = createCategory("cat", 1L, Integer.MAX_VALUE);
        Gift form = new Gift();
        form.setName(GIFT_NAME);
        form.setDescription(GIFT_DESCRIPTION);
        form.addLink(URL);
        form.setUserId(testAccount.getId());
        List<Long> idList = Arrays.asList(1L, 2L);
        form.setEngines(Collections.singleton(createSearchEngine(NAME, "link", "icon")));
        form.setCategory(cat);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(categoryServiceMock.findByName(anyString())).thenReturn(cat);
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isCreated());
        verify(giftServiceMock, times(1)).create(any(Gift.class));
        verify(categoryServiceMock, times(1)).findByName(anyString());
    }

    @Test
    public void createGiftNotFamily() throws Exception {
        Gift form = new Gift();
        form.setName(GIFT_NAME);
        form.setDescription(GIFT_DESCRIPTION);
        form.addLink(URL);
        form.setUserId(TestUtil.ADMIN_RANDOM_ID);
        List<Long> idList = Arrays.asList(1L, 2L);
        form.setEngines(Collections.singleton(createSearchEngine(NAME, "link", "icon")));
        form.setCategory(new Category("cat2"));
        when(accSrvMock.findById(TestUtil.ADMIN_RANDOM_ID)).thenReturn(TestUtil.createAdminAccount());
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createGiftEmptyName() throws Exception {
        GiftForm form = new GiftForm();
        form.setLink(URL);
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createGiftAccountNotFound() throws Exception {
        Gift form = new Gift();
        form.setName(GIFT_NAME);
        form.setDescription(GIFT_DESCRIPTION);
        form.addLink(URL);
        form.setUserId(TestUtil.ADMIN_RANDOM_ID);
        when(accSrvMock.findById(TestUtil.ADMIN_RANDOM_ID)).thenThrow(AccountNotFoundException.class);
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createGiftAccountIdNotFound() throws Exception {
        Gift form = new Gift();
        form.setName(GIFT_NAME);
        form.setDescription(GIFT_DESCRIPTION);
        form.addLink(URL);
        when(accSrvMock.findById(TestUtil.ADMIN_RANDOM_ID)).thenThrow(AccountNotFoundException.class);
        giftsRestCtrl.perform(post(API_GIFT_CREATE).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void editGiftSuccess() throws Exception {
        Category cat = createCategory("cat", 1l, Integer.MAX_VALUE);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("Name");
        gift.setUserId(testAccount.getId());
        Gift form = new Gift();
        form.setId(1L);
        form.setName(GIFT_NAME);
        form.setDescription(GIFT_DESCRIPTION);
        form.addLink(URL);
        form.setImageData("DATA");
        Set<SearchEngine> idList = Collections.singleton(createSearchEngine(NAME, "link", "icon"));
        form.setEngines(idList);
        form.setCategory(cat);
        Set<SearchEngine> engines = new HashSet<>();
        engines.add(createSearchEngine(NAME, "link", "icon"));
        when(giftServiceMock.findById(1L)).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        when(searchEngineServiceMock.getSearchEngines(Collections.singletonList(1L)))
                .thenReturn(engines);
        when(categoryServiceMock.save(any(Category.class))).then(returnsFirstArg());
        when(categoryServiceMock.findByName(anyString())).thenReturn(cat);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        MvcResult mvcResult = giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Gift result = TestUtil.convertJsonToObject(contentAsString, Gift.class);
        verify(giftServiceMock, times(1)).update(any(Gift.class));
        verify(categoryServiceMock, times(1)).findByName(anyString());
        verify(giftServiceMock, times(1)).updateGiftImage(any(Gift.class), anyString());
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
        gift.setUserId(OTHER_USER);
        when(giftServiceMock.findById(1L)).thenReturn(gift);
        Account giftOwner = TestUtil.createAccount();
        giftOwner.setId(OTHER_USER);
        when(accSrvMock.findById(OTHER_USER)).thenReturn(giftOwner);
        giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isConflict());
    }

    @Test
    public void editGiftAccountNotFound() throws Exception {
        GiftForm form = new GiftForm();
        form.setId(1L);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setUserId("other");
        gift.setUserId(OTHER_USER);
        when(giftServiceMock.findById(1L)).thenReturn(gift);
        when(accSrvMock.findById(OTHER_USER)).thenThrow(AccountNotFoundException.class);
        giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isConflict());
    }


    @Test
    public void getGiftsAccountNotFound() throws Exception {
        when(accSrvMock.findById(anyString())).thenThrow(AccountNotFoundException.class);
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
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(giftServiceMock.findAllByUser(testAccount.getId(), false)).thenReturn(expected);
        when(giftServiceMock.toGiftTreeMap(anyList(), anyBoolean())).thenCallRealMethod();
        when(accSrvMock.isAccountGroupMember(testAccount)).thenReturn(true);
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getId())).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(testAccount.getId()));
        assertTrue(contentAsString.contains(NAME));
    }

    @Test
    public void getClaimedGiftsAccount() throws Exception {
        Account account = TestUtil.createAccount();
        account.setId(account.getId() + 1);
        Gift gift = TestUtil.createGift(1L, testAccount);
        Gift gift2 = TestUtil.createGift(2L, account);
        Gift gift3 = TestUtil.createGift(3L, account);
        List<Gift> giftList = Arrays.asList(gift, gift2, gift3);
        Map<String, List<Gift>> giftMap = giftList.stream().collect(Collectors.groupingBy(Gift::getUserId));
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(accSrvMock.findByUsername(account.getId())).thenReturn(Optional.of(account));
        when(giftServiceMock.findAllClaimedByCurrentUser()).thenReturn(giftMap);
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_CLAIMED)).andExpect(status().isOk()).andReturn();
        Map<String, List> stringListMap = TestUtil.convertJsonToHashMap(mvcResult.getResponse().getContentAsString(), String.class, List.class);
        assertEquals(2, stringListMap.keySet().size());
        assertEquals(2, stringListMap.get(account.getId()).size());
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
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(giftServiceMock.findAllByUser(testAccount.getId(), false)).thenReturn(expected);
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
        when(accSrvMock.findByUsername(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(giftServiceMock.findAllByUser(testAccount.getId(), false)).thenReturn(expected);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        giftsRestCtrl.perform(get(API_GIFT_LIST + "/" + testAccount.getId())).andExpect(status().is4xxClientError());
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
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        giftsRestCtrl.perform(
                put(API_GIFT_CLAIM + gift.getId()))
                .andExpect(status().isOk());
        gift.setClaimed(testAccount);
        verify(giftServiceMock, times(1)).update(gift);
    }

    @Test
    public void claimGiftAccountNotFound() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.empty());
        giftsRestCtrl.perform(
                put(API_GIFT_CLAIM + 1))
                .andExpect(status().isNotFound());
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
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        giftsRestCtrl.perform(
                put(API_GIFT_UNCLAIM + gift.getId()))
                .andExpect(status().isOk());
        gift.setClaimed(null);
        verify(giftServiceMock, times(1)).update(gift);
    }

    @Test
    public void unClaimGiftAccountNotFound() throws Exception {
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.empty());
        giftsRestCtrl.perform(
                put(API_GIFT_UNCLAIM + 1))
                .andExpect(status().isNotFound());
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
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
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
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        giftsRestCtrl.perform(
                put(API_GIFT_CLAIM + gift.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void claimGiftAlreadyClaimed() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        Account claimed = TestUtil.createAccount(JOHN, NAME);
        owner.setId("2");
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        gift.setClaimed(claimed);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
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
        gift.setUserId(testAccount.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        giftsRestCtrl.perform(
                put(API_GIFT_COMPLETE + gift.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void completeGiftNoUser() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(OTHER_USER);
        Account giftOwner = TestUtil.createAccount();
        giftOwner.setId(OTHER_USER);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        doThrow(AccountNotFoundException.class).when(eventServiceMock).addEvent(gift, AppEventType.REALISED);
        when(accSrvMock.findById(OTHER_USER)).thenReturn(giftOwner);
        when(accSrvMock.findByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        giftsRestCtrl.perform(
                put(API_GIFT_COMPLETE + gift.getId()))
                .andExpect(status().is4xxClientError());
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
        Account giftOwner = TestUtil.createAccount();
        giftOwner.setId(OTHER_USER);
        when(accSrvMock.findById(OTHER_USER)).thenReturn(giftOwner);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        giftsRestCtrl.perform(
                put(API_GIFT_UNDO_COMPLETE + gift.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void undoCompleteGiftEventAccountError() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        gift.setStatus(GiftStatus.REALISED);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        doThrow(AccountNotFoundException.class).when(eventServiceMock).addEvent(gift, AppEventType.REALISED);
        giftsRestCtrl.perform(
                put(API_GIFT_UNDO_COMPLETE + gift.getId()))
                .andExpect(status().isOk());
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
        Account giftOwner = TestUtil.createAccount();
        giftOwner.setId(OTHER_USER);
        when(accSrvMock.findById(OTHER_USER)).thenReturn(giftOwner);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + "/1")).andExpect(status().is4xxClientError());
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
    public void deleteClaimedGiftNoNotifications() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        Account claimed = TestUtil.createAccount();
        gift.setClaimed(claimed);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
    }

    @Test
    public void deleteClaimedGiftWithNotifications() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        Account claimed = TestUtil.createAccount();
        claimed.setNotifications(true);
        gift.setClaimed(claimed);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
        verify(mailServiceMock, times(1)).notifyAboutGiftRemoved(gift);
    }

    @Test
    public void deleteClaimedGiftWithMailerError() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        Account claimed = TestUtil.createAccount();
        claimed.setNotifications(true);
        gift.setClaimed(claimed);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        doThrow(MessagingException.class).when(mailServiceMock).notifyAboutGiftRemoved(gift);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isInternalServerError());
    }


    @Test
    public void deleteGiftAsKidAdminSuccess() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId(OTHER_USER);
        owner.setType(AccountType.KID);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        Group group = new Group();
        group.getAdmins().add(testAccount);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accSrvMock.findById(owner.getId())).thenReturn(owner);
        when(accSrvMock.isAccountGroupMember(owner)).thenReturn(true);
        when(accSrvMock.isKidAdmin(owner)).thenReturn(true);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
    }

    @Test
    public void deleteGiftAsGiftCreatorSuccess() throws Exception {
        Account owner = TestUtil.createAccount(JOHN, DOE);
        owner.setId(OTHER_USER);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(owner.getId());
        gift.setCreatedBy(testAccount.getId());
        Group group = new Group();
        group.getAdmins().add(testAccount);
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accSrvMock.findById(owner.getId())).thenReturn(owner);
        when(accSrvMock.isAccountGroupMember(owner)).thenReturn(true);
        when(accSrvMock.isKidAdmin(owner)).thenReturn(true);
        giftsRestCtrl.perform(delete(API_GIFT_DELETE + gift.getId())).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).delete(gift);
    }

    //TODO needs reenable after import is done and fixed
//    @Test
//    public void importGifts() throws Exception {
//        Category category = new Category();
//        category.setName("category");
//        category.setId(1L);
//        when(categoryServiceMock.findByName("category")).thenReturn(category);
//        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
//        URL fileURL = getClass().getResource("sampleImport.xls");
//        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
//                getClass().getResourceAsStream("sampleImport.xls"));
//        giftsRestCtrl.perform(MockMvcRequestBuilders.multipart(API_GIFT_IMPORT).file(mockMultipartFile)).andExpect(status().isOk());
//        verify(categoryServiceMock, times(2)).save(any(Category.class));
//        verify(giftServiceMock, times(5)).create(any(Gift.class));
//    }

    //TODO needs reenable after import is done and fixed
//    @Test
//    public void importGiftsNotFamilyAdmin() throws Exception {
//        Account account = TestUtil.createAccount("John", "Doe");
//        Group group = new Group();
//        group.getMembers().add(account);
//        group.getAdmins().add(account);
//        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
//        when(accSrvMock.findById(account.getUsername())).thenReturn(account);
//        when(groupServiceMock.getGroup(account)).thenReturn(Optional.of(group));
//        URL fileURL = getClass().getResource("sampleImport.xls");
//        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
//                getClass().getResourceAsStream("sampleImport.xls"));
//        giftsRestCtrl.perform(MockMvcRequestBuilders.multipart(API_GIFT_IMPORT).file(mockMultipartFile).param("user", account.getUsername())).andExpect(status().isBadRequest());
//    }

//    @Test
//    public void importGiftsNoFamily() throws Exception {
//        Account account = TestUtil.createAccount("John", "Doe");
//        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
//        when(accSrvMock.findById(account.getUsername())).thenReturn(account);
//        URL fileURL = getClass().getResource("sampleImport.xls");
//        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
//                getClass().getResourceAsStream("sampleImport.xls"));
//        giftsRestCtrl.perform(MockMvcRequestBuilders.multipart(API_GIFT_IMPORT).file(mockMultipartFile).param("user", account.getUsername())).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void importGiftsNoAccount() throws Exception {
//        Account account = TestUtil.createAccount("John", "Doe");
//        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
//        when(accSrvMock.findById(account.getUsername())).thenThrow(AccountNotFoundException.class);
//        URL fileURL = getClass().getResource("sampleImport.xls");
//        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
//                getClass().getResourceAsStream("sampleImport.xls"));
//        giftsRestCtrl.perform(MockMvcRequestBuilders.multipart(API_GIFT_IMPORT).file(mockMultipartFile).param("user", account.getUsername())).andExpect(status().isBadRequest());
//    }


    @Test
    public void getTemplateTest() throws Exception {
        when(responseMock.getOutputStream()).thenReturn(outputStreamMock);
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_TEMPLATE)).andExpect(status().isOk()).andReturn();
        String contentType = mvcResult.getResponse().getContentType();
        byte[] contentAsByteArray = mvcResult.getResponse().getContentAsByteArray();
        String content = mvcResult.getResponse().getHeader("Content-Disposition");
        assertEquals("application/vnd.ms-excel", contentType);
        assertNotNull(content);
        assertTrue(contentAsByteArray.length > 0);
    }

    @Test
    public void allowedCategory() throws Exception {
        giftsRestCtrl.perform(get(API_GIFT_ALLOWED_CATEGORY).param("category", "Category")).andExpect(status().isOk());
    }

    @Test
    public void notAllowedCategory() throws Exception {
        giftsRestCtrl.perform(get(API_GIFT_ALLOWED_CATEGORY).param("category", "other")).andExpect(status().isConflict());
    }

    @Test
    public void giftPictureGiftNotFound() throws Exception {
        giftsRestCtrl.perform(get(API_GIFT_PICTURE + 1L)).andExpect(status().isNotFound());
    }

    @Test
    public void giftPictureAccountNotFound() throws Exception {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(testAccount.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenThrow(AccountNotFoundException.class);
        giftsRestCtrl.perform(get(API_GIFT_PICTURE + 1L)).andExpect(status().isNotFound());
    }

    @Test
    public void giftPictureAccountCannotView() throws Exception {
        Account account = TestUtil.createAccount();
        account.setId(account.getId() + 1);
        Group group = new Group();
        group.addMember(account);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(account.getId());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(account.getId())).thenReturn(account);
        giftsRestCtrl.perform(get(API_GIFT_PICTURE + 1L)).andExpect(status().isForbidden());
    }

    @Test
    public void giftPictureIsGroupMember() throws Exception {
        Account account = TestUtil.createAccount();
        account.setId(account.getId() + 1);
        Group group = new Group();
        group.addMember(account);
        group.addMember(testAccount);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(account.getId());
        gift.setImage(new GiftImage());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(account.getId())).thenReturn(account);
        giftsRestCtrl.perform(get(API_GIFT_PICTURE + 1L)).andExpect(status().isOk());
    }

    @Test
    public void giftPicturePublicList() throws Exception {
        Account account = TestUtil.createAccount();
        account.setId(account.getId() + 1);
        account.setPublicList(true);
        Group group = new Group();
        group.addMember(account);
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName(NAME);
        gift.setUserId(account.getId());
        gift.setImage(new GiftImage());
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(account.getId())).thenReturn(account);
        when(securityMock.getAuthentication()).thenReturn(annonymousTokenMock);
        giftsRestCtrl.perform(get(API_GIFT_PICTURE + 1L)).andExpect(status().isOk());
    }


}
