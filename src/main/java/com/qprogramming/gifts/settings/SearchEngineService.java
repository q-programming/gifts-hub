package com.qprogramming.gifts.settings;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by XE050991499 on 2017-03-21.
 */
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
        for (SearchEngine searchEngine : searchEngines) {
            SearchEngine engine;
            if (searchEngine.getId() == null) {//new engine
                engine = new SearchEngine();
            } else {
                engine = searchEngineRepository.findById(searchEngine.getId());

            }
            if (engine != null) {
                engine.setName(searchEngine.getName());
                engine.setIcon(searchEngine.getIcon());
                engine.setSearchString(searchEngine.getSearchString());
                searchEngineRepository.save(searchEngine);
            }
        }
    }
}
