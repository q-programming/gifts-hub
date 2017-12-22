package com.qprogramming.gifts.api.manage;

import com.qprogramming.gifts.MockSecurityContext;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.Roles;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryDTO;
import com.qprogramming.gifts.gift.category.CategoryService;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.settings.Settings;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Khobar on 20.03.2017.
 */
public class AppRestControllerTest {

    public static final String EN = "en";
    private static final String API_APPLICATION_SETTINGS = "/api/app/settings";
    private static final String API_APPLICATION_SETUP = "/api/app/setup";
    private static final String API_APPLICATION_SEARCH_ENGINES = "/api/app/search-engines";
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

    private Account testAccount;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        AppRestController mngCtrl = new AppRestController(propertyServiceMock, searchEngineServiceMock, mailServiceMock, categoryServiceMock, giftServiceMock);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
        this.manageRestController = MockMvcBuilders.standaloneSetup(mngCtrl).build();
    }

    @Test
    public void changeSettingsLanguage() throws Exception {
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
    public void changeSettingsAddSearchEngine() throws Exception {
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
    public void setApplicationSettingsBadAuth() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        Settings settings = new Settings();
        manageRestController.perform(
                post(API_APPLICATION_SETTINGS)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void applicationSettingsBadAuth() throws Exception {
        testAccount.setRole(Roles.ROLE_USER);
        manageRestController.perform(get(API_APPLICATION_SETTINGS)).andExpect(status().isForbidden());
    }

    @Test
    public void applicationSettingsDataRecived() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getDefaultLang()).thenReturn(EN);
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETTINGS)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Settings settings = TestUtil.convertJsonToObject(contentAsString, Settings.class);
        assertEquals(EN, settings.getLanguage());
    }

    @Test
    public void applicationSettingsCategories() throws Exception {
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
    public void applicationSettingsUpdateCategoryPriorities() throws Exception {
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
    public void getAllSearchEngines() throws Exception {
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
    public void setupNeededEmptyURL() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn(null);
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETUP)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Boolean result = TestUtil.convertJsonToObject(contentAsString, Boolean.class);
        assertTrue(result);

    }

    @Test
    public void setupNeededEmptySearchEngines() throws Exception {
        testAccount.setRole(Roles.ROLE_ADMIN);
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn("link");
        when(searchEngineServiceMock.getAllSearchEngines()).thenReturn(Collections.emptyList());
        MvcResult mvcResult = manageRestController.perform(get(API_APPLICATION_SETUP)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Boolean result = TestUtil.convertJsonToObject(contentAsString, Boolean.class);
        assertTrue(result);
    }

    @Test
    public void setupNotNeeded() throws Exception {
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

    private Category createCategory(String name, Long id, Integer priority) {
        Category category = new Category();
        category.setName(name);
        category.setId(id);
        category.setPriority(priority);
        return category;
    }


}