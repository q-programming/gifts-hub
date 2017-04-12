package com.qprogramming.gifts.settings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.qprogramming.gifts.TestUtil.createSearchEngine;
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
        when(searchEngineRepositoryMock.findAll()).thenReturn(dbContents);
        searchEngineService.updateSearchEngines(passed);
        Set<SearchEngine> expectedUpdateIntactOrNew = new HashSet<>();
        expectedUpdateIntactOrNew.add(newEngine);
        expectedUpdateIntactOrNew.add(intactEngine);
        expectedUpdateIntactOrNew.add(editedEngineNew);
        List<SearchEngine> expectedDelete = new ArrayList<>();
        expectedDelete.add(deletedEngine);
        verify(searchEngineRepositoryMock, times(1)).save(expectedUpdateIntactOrNew);
        verify(searchEngineRepositoryMock, times(1)).delete(expectedDelete);
    }
}