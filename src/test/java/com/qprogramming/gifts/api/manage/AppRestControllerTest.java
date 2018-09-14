package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.Roles;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryDTO;
import com.qprogramming.gifts.gift.category.CategoryService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.settings.Settings;
import com.qprogramming.gifts.support.ResultData;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.qprogramming.gifts.settings.Settings.APP_URL;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 20.03.2017.
 */
public class AppRestControllerTest {

    public static final String EN = "en";
    private static final String API_APPLICATION_SETTINGS = "/api/app/settings";
    private static final String API_APPLICATION_ADD_ADMIN = "/api/app/add-admin";
    private static final String API_APPLICATION_REMOVE_ADMIN = "/api/app/remove-admin";
    private static final String API_APPLICATION_SETUP = "/api/app/setup";
    private static final String API_APPLICATION_SEARCH_ENGINES = "/api/app/search-engines";
    public static final String API_APPLICATION_REMOVE_CATEGORY = "/api/app/remove-category";
    public static final String API_APPLICATION_UPDATE_CATEGORY = "/api/app/update-category";
    private MockMvc manageRestController;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private SearchEngineService searchEngineServiceMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    @Mock
    private MailService mailServiceMock;
    @Mock
    private CategoryService categoryServiceMock;
    @Mock
    private GiftService giftServiceMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private MessagesService msgSrvMock;

    private Account testAccount;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(msgSrvMock.getMessage(anyString())).thenReturn("MESSAGE");
        SecurityContextHolder.setContext(securityMock);
        AppRestController mngCtrl = new AppRestController(propertyServiceMock, searchEngineServiceMock, mailServiceMock, categoryServiceMock, giftServiceMock, accountServiceMock, msgSrvMock);
        this.manageRestController = MockMvcBuilders.standaloneSetup(mngCtrl).build();
    }

    @Test
    public void changeSettingsLanguageTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Settings settings = new Settings();
        settings.setLanguage(EN);
        manageRestController.perform(
                post(API_APPLICATION_SETTINGS)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isOk());
        verify(propertyServiceMock, times(1)).update(Settings.APP_DEFAULT_LANG, EN);
    }

    @Test
    public void changeSettingsAddSearchEngineTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        SearchEngine engine = new SearchEngine();
        engine.setName("google");
        engine.setSearchString("http://www.google.com/search?q=");
        engine.setIcon("icon");
        Settings settings = new Settings();
        settings.addSearchEngine(engine);
        manageRestController.perform(
                post(API_APPLICATION_SETTINGS)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isOk());
        verify(searchEngineServiceMock, times(1)).updateSearchEngines(settings.getSearchEngines());
    }

    @Test
    public void setApplicationSettingsBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        Settings settings = new Settings();
        manageRestController.perform(
                post(API_APPLICATION_SETTINGS)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void applicationSettingsBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        manageRestController.perform(get(API_APPLICATION_SETTINGS)).andExpect(status().isForbidden());
    }

    @Test
    public void applicationSettingsDataRecivedTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getDefaultLang()).thenReturn(EN);
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETTINGS)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Settings settings = TestUtil.convertJsonToObject(contentAsString, Settings.class);
        assertEquals(EN, settings.getLanguage());
    }

    @Test
    public void applicationSettingsCategoriesTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getDefaultLang()).thenReturn(EN);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        Category category2 = createCategory("category2", 2L, 0);
        Category category3 = createCategory("category3", 3L, Integer.MIN_VALUE);
        Category category4 = createCategory("category4", 4L, 10);
        Gift gift1 = TestUtil.createGift(1L, testAccount);
        gift1.setCategory(category1);
        Gift gift2 = TestUtil.createGift(1L, testAccount);
        gift2.setCategory(category1);
        Gift gift3 = TestUtil.createGift(1L, testAccount);
        gift3.setCategory(category2);
        Gift gift4 = TestUtil.createGift(1L, testAccount);
        gift4.setCategory(category3);
        Gift gift5 = TestUtil.createGift(1L, testAccount);
        when(giftServiceMock.findAll()).thenReturn(Arrays.asList(gift1, gift2, gift3, gift4, gift5));
        when(categoryServiceMock.findAll()).thenReturn(Arrays.asList(category1, category2, category3, category4));
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETTINGS)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Settings settings = TestUtil.convertJsonToObject(contentAsString, Settings.class);
        assertEquals(4, settings.getCategories().size());
        assertEquals(category1, settings.getCategories().get(0).getCategory());
        assertEquals(new Long(2), settings.getCategories().get(0).getCount());
    }

    @Test
    public void applicationSettingsUpdateCategoryPrioritiesTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Settings settings = new Settings();
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        Category category2 = createCategory("category2", 2L, 0);
        Category category3 = createCategory("category3", 3L, Integer.MIN_VALUE);
        Category category4 = createCategory("category4", 4L, 10);
        settings.setCategories(Stream.of(category3, category2, category1, category4)
                .map(category -> new CategoryDTO(category, 0L))
                .collect(Collectors.toList()));
        manageRestController.perform(post(API_APPLICATION_SETTINGS)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(settings))
        ).andExpect(status().isOk());
        verify(categoryServiceMock, times(1)).update(anyListOf(Category.class));
    }

    @Test
    public void removeCategoryBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        manageRestController.perform(post(API_APPLICATION_REMOVE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isForbidden());
    }


    @Test
    public void removeCategoryNotFoundTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        manageRestController.perform(post(API_APPLICATION_REMOVE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isNotFound());
    }


    @Test
    public void removeCategoryTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        when(categoryServiceMock.findById(category1.getId())).thenReturn(category1);
        manageRestController.perform(post(API_APPLICATION_REMOVE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isOk());
        verify(giftServiceMock, times(1)).removeCategory(category1);
        verify(categoryServiceMock, times(1)).remove(category1);
    }

    @Test
    public void editCategoryBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        manageRestController.perform(post(API_APPLICATION_UPDATE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isForbidden());
    }


    @Test
    public void editCategoryNotFoundTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        manageRestController.perform(post(API_APPLICATION_UPDATE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void editCategoryTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        Category category1 = createCategory("category1", 1L, Integer.MAX_VALUE);
        when(categoryServiceMock.findById(category1.getId())).thenReturn(category1);
        manageRestController.perform(post(API_APPLICATION_UPDATE_CATEGORY)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(category1))
        ).andExpect(status().isOk());
        verify(categoryServiceMock, times(1)).save(category1);
    }


    @Test
    public void getAllSearchEnginesTest() throws Exception {
        List<SearchEngine> expected = new ArrayList<>();
        expected.add(TestUtil.createSearchEngine("google", "link", "icon"));
        expected.add(TestUtil.createSearchEngine("bing", "link", "icon"));
        when(searchEngineServiceMock.getAllSearchEngines()).thenReturn(expected);
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SEARCH_ENGINES)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<SearchEngine> result = TestUtil.convertJsonToList(contentAsString, List.class, SearchEngine.class);
        assertEquals(expected, result);
    }

    @Test
    public void setupNeededEmptyURLTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn(null);
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETUP)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Boolean result = TestUtil.convertJsonToObject(contentAsString, Boolean.class);
        assertTrue(result);

    }

    @Test
    public void setupNeededEmptySearchEnginesTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn("link");
        when(searchEngineServiceMock.getAllSearchEngines()).thenReturn(Collections.emptyList());
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETUP)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Boolean result = TestUtil.convertJsonToObject(contentAsString, Boolean.class);
        assertTrue(result);
    }

    @Test
    public void setupNotNeededTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn("link");
        SearchEngine engine = new SearchEngine();
        engine.setId(1L);
        when(searchEngineServiceMock.getAllSearchEngines()).thenReturn(Collections.singletonList(engine));
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETUP)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Boolean result = TestUtil.convertJsonToObject(contentAsString, Boolean.class);
        assertFalse(result);
    }

    @Test
    public void addAdminBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        manageRestController.perform(
                put(API_APPLICATION_ADD_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getId()))
                .andExpect(status().isForbidden());
    }


    @Test
    public void addAdminUserNotFoundTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        manageRestController.perform(
                put(API_APPLICATION_ADD_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getId()))
                .andExpect(status().isNotFound());

    }

    @Test
    public void addAdminTest() throws Exception {
        Account account = TestUtil.createAccount();
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        manageRestController.perform(
                put(API_APPLICATION_ADD_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(account.getId()))
                .andExpect(status().isOk());
        verify(accountServiceMock, times(1)).update(account);
        assertTrue(account.getIsAdmin());
    }


    @Test
    public void removeAdminBadAuthTest() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        manageRestController.perform(
                put(API_APPLICATION_REMOVE_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getId()))
                .andExpect(status().isForbidden());
    }


    @Test
    public void removedminUserNotFoundTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        manageRestController.perform(
                put(API_APPLICATION_REMOVE_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getId()))
                .andExpect(status().isNotFound());

    }

    @Test
    public void removeAdminTest() throws Exception {
        Account account = TestUtil.createAccount();
        account.setRole(Roles.ROLE_ADMIN);
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        when(accountServiceMock.findUsers()).thenReturn(Arrays.asList(account, testAccount));
        manageRestController.perform(
                put(API_APPLICATION_REMOVE_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(account.getId()))
                .andExpect(status().isOk());
        verify(accountServiceMock, times(1)).update(account);
        assertFalse(account.getIsAdmin());
    }

    @Test
    public void removeLastAdminTest() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accountServiceMock.findUsers()).thenReturn(Collections.singletonList(testAccount));
        MvcResult mvcResult = manageRestController.perform(
                put(API_APPLICATION_REMOVE_ADMIN)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getId()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        assertTrue(testAccount.getIsAdmin());
        assertTrue(contentAsString.contains(ResultData.Code.ERROR.toString()));
    }

    private Category createCategory(String name, Long id, Integer priority) {
        Category category = new Category();
        category.setName(name);
        category.setId(id);
        category.setPriority(priority);
        return category;
    }


}