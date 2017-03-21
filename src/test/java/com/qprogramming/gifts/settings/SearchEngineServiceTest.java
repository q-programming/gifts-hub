package com.qprogramming.gifts.settings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by XE050991499 on 2017-03-21.
 */
public class SearchEngineServiceTest {

    private SearchEngineService searchEngineService;
    @Mock
    private SearchEngineRepository searchEngineRepositoryMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        searchEngineService = new SearchEngineService(searchEngineRepositoryMock);
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
        SearchEngine engine = createSearchEngine("google", "link", "icon");
        SearchEngine engine2 = createSearchEngine("bing", "link", "icon");
        engine2.setId(1L);
        SearchEngine engine3 = createSearchEngine("yahoo", "link", "icon");
        engine3.setId(2L);
        List<SearchEngine> passed = new ArrayList<>();
        passed.add(engine);
        passed.add(engine2);
        List<SearchEngine> dbContents = new ArrayList<>();
        dbContents.add(engine2);
        dbContents.add(engine3);
        when(searchEngineRepositoryMock.findAll()).thenReturn(dbContents);
        searchEngineService.updateSearchEngines(passed);
        verify(searchEngineRepositoryMock, times(1)).save(anyCollectionOf(SearchEngine.class));
        verify(searchEngineRepositoryMock, times(1)).delete(anyCollectionOf(SearchEngine.class));
        verify(searchEngineRepositoryMock, times(1)).save(any(SearchEngine.class));
    }

    private SearchEngine createSearchEngine(String name, String link, String icon) {
        SearchEngine engine = new SearchEngine();
        engine.setName(name);
        engine.setSearchString(link);
        engine.setIcon(icon);
        return engine;
    }

}