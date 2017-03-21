package com.qprogramming.gifts.settings;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SearchEngineService {

    private SearchEngineRepository searchEngineRepository;

    public SearchEngineService(SearchEngineRepository searchEngineRepository) {
        this.searchEngineRepository = searchEngineRepository;
    }

    public List<SearchEngine> getAllSearchEngines() {
        return searchEngineRepository.findAll();
    }

    public void updateSearchEngines(List<SearchEngine> searchEngines) {

        List<SearchEngine> allSearchEngines = searchEngineRepository.findAll();
        List<SearchEngine> stillExisting = allSearchEngines.stream().filter(searchEngines::contains).collect(Collectors.toList());
        searchEngines.removeAll(stillExisting);
        allSearchEngines.removeAll(stillExisting);
        //save allSearchEngines stillExisting and update them if needed
        searchEngineRepository.save(stillExisting);//TODO save passed version instead of old one
        //remove not present in passed list
        searchEngineRepository.delete(allSearchEngines);
        //iterate over rest( only new should remain)
        for (SearchEngine searchEngine : searchEngines) {
            SearchEngine engine = new SearchEngine();
            engine.setName(searchEngine.getName());
            engine.setIcon(searchEngine.getIcon());
            engine.setSearchString(searchEngine.getSearchString());
            searchEngineRepository.save(searchEngine);

        }
    }
}
