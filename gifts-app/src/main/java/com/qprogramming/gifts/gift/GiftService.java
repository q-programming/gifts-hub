package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.support.Utils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.settings.Settings.APP_GIFT_AGE;
import static com.qprogramming.gifts.support.Utils.not;

@Service
public class GiftService {

    private GiftRepository giftRepository;
    private PropertyService propertyService;

    public GiftService(GiftRepository giftRepository, PropertyService propertyService) {
        this.giftRepository = giftRepository;
        this.propertyService = propertyService;
    }

    /**
     * Converts gifts list to TreeMap based on their's category
     * After map was build each list is sorted
     *
     * @param gifts list of gifts to put into TreeMap
     * @return TreeMap with gifts sorted by categories ( based on priorities)
     */
    public Map<Category, List<Gift>> toGiftTreeMap(List<Gift> gifts, boolean sort) {
        Map<Category, List<Gift>> result = new TreeMap<>();
        gifts.forEach(gift -> {
            result.computeIfAbsent(gift.getCategory(), k -> new ArrayList<>());
            result.get(gift.getCategory()).add(gift);
        });
        if (sort) {
            result.values().forEach(GiftComparator::sortGiftList);
        }
        return result;
    }

    public Gift create(Gift gift) {
        gift.setCreated(new Date());
        return giftRepository.save(gift);
    }

    /**
     * Returns a tree map of Category,GiftList for currently logged in user
     *
     * @return Map&lt;Category,List&lt;Gift&gt;&gt;
     */
    public Map<Category, List<Gift>> findAllByCurrentUser() {
        List<Gift> giftList = getCurrentUserGifts();
        return toGiftTreeMap(giftList, false);
    }

    private List<Gift> getCurrentUserGifts() {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        return giftRepository.findByUserIdOrderByCreatedDesc(Utils.getCurrentAccountId()).stream().filter(not(Gift::isHidden)).peek(gift -> {
            setGiftStatus(gift, giftAge);
            gift.setClaimed(null);//remove claimed as current user shouldn't see it
        }).collect(Collectors.toList());
    }

    private List<Gift> getUserGifts(String id) {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        return giftRepository.findByUserIdOrderByCreatedDesc(id).stream().peek(gift -> setGiftStatus(gift, giftAge)).collect(Collectors.toList());
    }


    /**
     * Returns a tree map of Category,GiftList for user
     *
     * @param id id of user for which gift tree map will be returned
     * @return Map&lt;Category,List&lt;Gift&gt;&gt;
     */
    public Map<Category, List<Gift>> findAllByUser(String id) {
        List<Gift> giftList = id.equals(Utils.getCurrentAccountId()) ?
                getCurrentUserGifts() : getUserGifts(id);
        boolean sort = !id.equals(Utils.getCurrentAccountId());
        return toGiftTreeMap(giftList, sort);
    }

    public int countAllByUser(String id) {
        return (int) giftRepository.findByUserIdOrderByCreatedDesc(id)
                .stream()
                .filter(gift -> gift.getStatus() == null || !GiftStatus.REALISED.equals(gift.getStatus()))
                .count();
    }

    public Gift findById(Long id) {
        return giftRepository.findById(id).orElse(null);
    }

    public Gift update(Gift gift) {
        return giftRepository.save(gift);
    }

    /**
     * Set gift status and potentially add it to realised category
     *
     * @param gift    gift which status will be checked
     * @param giftAge how long gift is considered new ( set via application management )
     */
    private void setGiftStatus(Gift gift, int giftAge) {
        if (!GiftStatus.REALISED.equals(gift.getStatus())) {
            Days giftDays = Days.daysBetween(new LocalDate(gift.getCreated()), new LocalDate());
            if (giftDays.getDays() < giftAge) {
                gift.setStatus(GiftStatus.NEW);
            }
        } else if (GiftStatus.REALISED.equals(gift.getStatus())) {
            Category category = new Category(Category.REALISED);
            category.setPriority(Integer.MIN_VALUE);
            gift.setCategory(category);
        }

    }

    public void delete(Gift gift) {
        giftRepository.delete(gift);
    }

    public void deleteUserGifts(Account account) {
        List<Gift> userGifts = giftRepository.findByUserIdOrderByCreatedDesc(account.getId());
        giftRepository.deleteAll(userGifts);
    }

    public void deleteClaims(Account account) {
        List<Gift> claimedGifts = giftRepository.findByClaimed(account);
        claimedGifts.forEach(gift -> gift.setClaimed(null));
        giftRepository.saveAll(claimedGifts);
    }

    public List<Gift> findAll() {
        return giftRepository.findAll();
    }

    public void removeCategory(Category category) {
        List<Gift> allByCategory = giftRepository.findAllByCategory(category);
        allByCategory.forEach(gift -> gift.setCategory(null));
        giftRepository.saveAll(allByCategory);
    }

    public void removeSearchEngine(SearchEngine searchEngine) {
        List<Gift> gifts = giftRepository.findByEngines(searchEngine);
        gifts.forEach(gift -> gift.getEngines().remove(searchEngine));
        giftRepository.saveAll(gifts);
    }
}
