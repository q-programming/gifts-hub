package com.qprogramming.gifts.settings;

import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.TestUtil;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftRepository;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.image.GiftImageRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static com.qprogramming.gifts.TestUtil.createSearchEngine;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by XE050991499 on 2017-03-21.
 */
public class SearchEngineServiceTest extends MockedAccountTestBase {

    private SearchEngineService searchEngineService;
    private GiftService giftService;
    @Mock
    private SearchEngineRepository searchEngineRepositoryMock;
    @Mock
    private GiftRepository giftRepositoryMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private GiftImageRepository imageRepositoryMock;


    @Before
    public void setUp() throws Exception {
        super.setup();
        this.giftService = new GiftService(giftRepositoryMock, propertyServiceMock, imageRepositoryMock);
        searchEngineService = new SearchEngineService(searchEngineRepositoryMock, giftService);
    }

    @Test
    public void getAllSearchEngines() throws Exception {
        SearchEngine engine = createSearchEngine("google", "link", "icon");
        SearchEngine engine2 = createSearchEngine("bing", "link", "icon");
        List<SearchEngine> expected = new ArrayList<>();
        expected.add(engine);
        expected.add(engine2);
        when(searchEngineRepositoryMock.findAll()).thenReturn(expected);
        List<SearchEngine> result = searchEngineService.getAllSearchEngines();
        assertEquals(expected, result);
    }

    @Test
    public void updateSearchEngines() throws Exception {
        SearchEngine newEngine = createSearchEngine("google", "link", "icon");

        SearchEngine intactEngine = createSearchEngine("bing", "link", "icon");
        intactEngine.setId(1L);

        SearchEngine editedEngine = createSearchEngine("yahoo", "link", "icon");
        editedEngine.setId(2L);

        SearchEngine editedEngineNew = createSearchEngine("edited_yahoo", "editedLink", "icon");
        editedEngineNew.setId(2L);

        SearchEngine deletedEngine = createSearchEngine("wolfram", "link", "icon");
        deletedEngine.setId(3L);

        //passed params
        List<SearchEngine> passed = new ArrayList<>();
        passed.add(newEngine);
        passed.add(intactEngine);
        passed.add(editedEngineNew);
        //Db contets
        List<SearchEngine> dbContents = new ArrayList<>();
        dbContents.add(intactEngine);
        dbContents.add(editedEngine);
        dbContents.add(deletedEngine);
        //gift
        Gift gift = TestUtil.createGift(1L, testAccount);
        gift.getEngines().add(deletedEngine);

        when(searchEngineRepositoryMock.findAll()).thenReturn(dbContents);
        when(giftRepositoryMock.findByEngines(deletedEngine)).thenReturn(Collections.singletonList(gift));
        searchEngineService.updateSearchEngines(passed);
        Set<SearchEngine> expectedUpdateIntactOrNew = new HashSet<>();
        expectedUpdateIntactOrNew.add(newEngine);
        expectedUpdateIntactOrNew.add(intactEngine);
        expectedUpdateIntactOrNew.add(editedEngineNew);
        List<SearchEngine> expectedDelete = new ArrayList<>();
        expectedDelete.add(deletedEngine);
        verify(searchEngineRepositoryMock, times(1)).saveAll(expectedUpdateIntactOrNew);
        verify(searchEngineRepositoryMock, times(1)).deleteAll(expectedDelete);
    }
}
