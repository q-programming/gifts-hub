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
import com.qprogramming.gifts.gift.link.Link;
import com.qprogramming.gifts.gift.link.LinkRepository;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GiftRestControllerTest {
    private static final String API_GIFT_CREATE = "/api/gift/create";
    private static final String API_GIFT_EDIT = "/api/gift/edit";
    private static final String API_GIFT_LIST = "/api/gift/user";
    private static final String API_GIFT_CLAIM = "/api/gift/claim/";
    private static final String API_GIFT_UNCLAIM = "/api/gift/unclaim/";
    private static final String API_GIFT_COMPLETE = "/api/gift/complete/";
    private static final String API_GIFT_UNDO_COMPLETE = "/api/gift/undo-complete/";
    private static final String API_GIFT_DELETE = "/api/gift/delete/";
    private static final String API_GIFT_IMPORT = "/api/gift/import";
    private static final String API_GIFT_TEMPLATE = "/api/gift/get-template";
    private static final String API_GIFT_ALLOWED_CATEGORY = "/api/gift/allowed-category";
    private static final String OTHER_USER = "OTHER_USER";
    private static final String JOHN = "John";
    private static final String DOE = "Doe";
    private static final String NAME = "name";
    private static final String TEMPLATE_XLS = "template.xls";
    public static final String NEW_URL = "newUrl";
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
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private ServletOutputStream outputStreamMock;
    @Mock
    private MockMultipartFile mockMultipartFile;
    @Mock
    private AppEventService eventServiceMock;
    @Mock
    private LinkRepository linkRepositoryMock;

    private Account testAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(messagesServiceMock.getMessage("gift.category.other", null, "", new Locale("en"))).thenReturn("Other");
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        GiftRestController giftsCtrl = new GiftRestController(accSrvMock, giftServiceMock, searchEngineServiceMock, categoryRepositoryMock, messagesServiceMock, familyServiceMock, eventServiceMock, linkRepositoryMock);
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

    @Test
    public void importGifts() throws Exception {
        Category category = new Category();
        category.setName("category");
        category.setId(1L);
        when(categoryRepositoryMock.findByName("category")).thenReturn(category);
        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
        URL fileURL = getClass().getResource("sampleImport.xls");
        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
                getClass().getResourceAsStream("sampleImport.xls"));
        giftsRestCtrl.perform(MockMvcRequestBuilders.fileUpload(API_GIFT_IMPORT).file(mockMultipartFile)).andExpect(status().isOk());
        verify(categoryRepositoryMock, times(2)).save(any(Category.class));
        verify(giftServiceMock, times(5)).create(any(Gift.class));
    }

    @Test
    public void importGiftsNotFamilyAdmin() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        Family family = new Family();
        family.getMembers().add(account);
        family.getAdmins().add(account);
        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
        when(accSrvMock.findByUsername(account.getUsername())).thenReturn(account);
        when(familyServiceMock.getFamily(account)).thenReturn(family);
        URL fileURL = getClass().getResource("sampleImport.xls");
        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
                getClass().getResourceAsStream("sampleImport.xls"));
        giftsRestCtrl.perform(MockMvcRequestBuilders.fileUpload(API_GIFT_IMPORT).file(mockMultipartFile).param("user", account.getUsername())).andExpect(status().isBadRequest());
    }

    @Test
    public void importGiftsNoFamily() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(giftServiceMock.create(any(Gift.class))).then(returnsFirstArg());
        when(accSrvMock.findByUsername(account.getUsername())).thenReturn(account);
        URL fileURL = getClass().getResource("sampleImport.xls");
        mockMultipartFile = new MockMultipartFile("file", fileURL.getFile(), "text/plain",
                getClass().getResourceAsStream("sampleImport.xls"));
        giftsRestCtrl.perform(MockMvcRequestBuilders.fileUpload(API_GIFT_IMPORT).file(mockMultipartFile).param("user", account.getUsername())).andExpect(status().isBadRequest());
    }


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
        MvcResult mvcResult = giftsRestCtrl.perform(get(API_GIFT_ALLOWED_CATEGORY).param("category", "other")).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultData resultData = TestUtil.convertJsonToObject(contentAsString, ResultData.class);
        assertEquals(resultData.code, ResultData.Code.ERROR);
    }

    @Test
    public void testUpdateGiftWithLinks() throws Exception {
        Gift gift = TestUtil.createGift(1L, testAccount);
        GiftForm form = new GiftForm();
        form.setName(gift.getName());
        form.setId(gift.getId());
        Link link = createLink(1L);
        Link link2 = createLink(2L);
        Link link3 = createLink(3L);
        Link link4 = createLink(4L);
        List<Link> linksCollection = new ArrayList<>(Arrays.asList(link, link2, link3, link4));
        Link emptyLink = new Link();
        Link newlink = createLink(null);
        Link updatedLink = createLink(4L);
        updatedLink.setUrl(NEW_URL);
        gift.setLinks(linksCollection);
        form.setLinks(Arrays.asList(link, link2, newlink, updatedLink,emptyLink));
        when(giftServiceMock.findById(gift.getId())).thenReturn(gift);
        when(accSrvMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(giftServiceMock.update(any(Gift.class))).then(returnsFirstArg());
        MvcResult mvcResult = giftsRestCtrl.perform(post(API_GIFT_EDIT).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Gift result = TestUtil.convertJsonToObject(contentAsString, Gift.class);
        assertEquals(4, result.getLinks().size());
        Optional<Link> updatedLinkOptional = result.getLinks().stream().filter(link1 -> link1.getId().equals(4L)).findFirst();
        assertTrue(updatedLinkOptional.isPresent());
        assertTrue(updatedLinkOptional.get().getUrl().equals(NEW_URL));
        verify(linkRepositoryMock, times(1)).delete(anyCollectionOf(Link.class));
        verify(linkRepositoryMock, times(1)).save(any(Link.class));
    }

    private Link createLink(Long id) {
        Link link = new Link("url");
        if (id != null) {
            link.setId(id);
        }
        return link;
    }
}
