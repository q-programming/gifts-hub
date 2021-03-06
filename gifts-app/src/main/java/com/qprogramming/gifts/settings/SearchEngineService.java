package com.qprogramming.gifts.settings;

import com.qprogramming.gifts.gift.GiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class SearchEngineService {

    private SearchEngineRepository searchEngineRepository;
    private GiftService giftService;

    @Autowired
    public SearchEngineService(SearchEngineRepository searchEngineRepository, GiftService giftService) {
        this.searchEngineRepository = searchEngineRepository;
        this.giftService = giftService;
    }

    public List<SearchEngine> getAllSearchEngines() {
        return searchEngineRepository.findAll();
    }

    public void updateSearchEngines(List<SearchEngine> searchEngines) {
        Set<SearchEngine> dbSearchEngines = new HashSet<>(searchEngineRepository.findAll());
        List<SearchEngine> toUpdate = dbSearchEngines.stream().filter(searchEngines::contains).collect(Collectors.toList());
        //remove updated items and replace them with new version from passed list
        dbSearchEngines.removeAll(toUpdate);
        dbSearchEngines.addAll(searchEngines);
        //collect items that will be deleted
        List<SearchEngine> toRemove = dbSearchEngines.stream().filter(o -> !searchEngines.contains(o)).collect(Collectors.toList());
        dbSearchEngines.removeAll(toRemove);
        //sanitze any gifts that might be using removed search engines
        toRemove.forEach(searchEngine -> {
            giftService.removeSearchEngine(searchEngine);
        });
        searchEngineRepository.deleteAll(toRemove);
        searchEngineRepository.saveAll(dbSearchEngines);
    }

    public SearchEngine findById(Long id) {
        return searchEngineRepository.findById(id).orElse(null);
    }

    public Set<SearchEngine> getSearchEngines(List<Long> searchEngines) {
        if (searchEngines != null) {
            return searchEngines.stream().map(this::findById).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
