package com.qprogramming.gifts.settings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        SearchEngine engine = new SearchEngine();
        engine.setName("google");
        engine.setSearchString("http://www.google.com/search?q=");
        engine.setIcon("icon");
        SearchEngine engine2 = new SearchEngine();
        engine2.setName("bing");
        engine2.setSearchString("http://www.bing.com/search?q=");
        engine2.setIcon("icon");
        List<SearchEngine> expected = new ArrayList<>();
        expected.add(engine);
        expected.add(engine2);
        when(searchEngineRepositoryMock.findAll()).thenReturn(expected);
        List<SearchEngine> result = searchEngineService.getAllSearchEngines();
        assertEquals(expected, result);
    }

    @Test
    public void updateSearchEngines() throws Exception {
        SearchEngine engine = new SearchEngine();
        engine.setName("google");
        engine.setSearchString("http://www.google.com/search?q=");
        engine.setIcon("icon");
        SearchEngine engine2 = new SearchEngine();
        engine2.setId(1L);
        engine2.setName("bing");
        engine2.setSearchString("http://www.bing.com/search?q=");
        engine2.setIcon("icon");
        List<SearchEngine> expected = new ArrayList<>();
        expected.add(engine);
        expected.add(engine2);
        when(searchEngineRepositoryMock.findById(1L)).thenReturn(engine2);
        searchEngineService.updateSearchEngines(expected);
        verify(searchEngineRepositoryMock,times(2)).save(any(SearchEngine.class));

    }

}