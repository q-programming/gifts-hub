package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Create gift. Additionally sets created by and date of creation
     *
     * @param gift gift to be persisted
     * @return persisted gift
     */
    public Gift create(Gift gift) {
        gift.setCreated(new Date());
        gift.setCreatedBy(Utils.getCurrentAccountId());
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
     * @return Map of Category - List of gifts for it
     */
    public Map<Category, List<Gift>> findAllByUser(String id) {
        List<Gift> giftList = id.equals(Utils.getCurrentAccountId()) ?
                getCurrentUserGifts() : getUserGifts(id);
        boolean sort = !id.equals(Utils.getCurrentAccountId());
        return toGiftTreeMap(giftList, sort);
    }

    /**
     * Count all gifts for user with ID
     *
     * @param id account id to be checked
     * @return count of all gifts for account
     */
    public int countAllByAccountId(String id) {
        return (int) giftRepository.findByUserIdOrderByCreatedDesc(id)
                .stream()
                .filter(gift -> gift.getStatus() == null || !GiftStatus.REALISED.equals(gift.getStatus()))
                .count();
    }

    /**
     * Find gift by id
     *
     * @param id id of gift to be found
     * @return found gift or null if it was not found
     */
    public Gift findById(Long id) {
        return giftRepository.findById(id).orElse(null);
    }

    /**
     * Updates passed gift.
     * If gift has other category ( with negative id, and empty name ) it will be set to null to omit any persisting issues
     *
     * @param gift Gift to be updated
     * @return updated gift
     */
    public Gift update(Gift gift) {
        if (hasOtherCategory(gift)) {
            gift.setCategory(null);
        }
        return giftRepository.save(gift);
    }

    /**
     * Check if gift has dummy other category , which has negative id ( Integer.MIN_VALUE) and empty name
     *
     * @param gift gift to be checked
     * @return true if gift has other category
     */
    private boolean hasOtherCategory(Gift gift) {
        return gift.getCategory() != null
                && gift.getCategory().getId() == Integer.MIN_VALUE
                && StringUtils.isBlank(gift.getCategory().getName());
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

    /**
     * Delete Gift
     *
     * @param gift gift to be deleted
     */
    public void delete(Gift gift) {
        giftRepository.delete(gift);
    }

    /**
     * Delete all user gifts ( used when deleting account )
     *
     * @param account account for which gifts will be found and deleted
     */
    public void deleteUserGifts(Account account) {
        List<Gift> userGifts = giftRepository.findByUserIdOrderByCreatedDesc(account.getId());
        giftRepository.deleteAll(userGifts);
    }

    /**
     * Delete all claims for account ( used when deleting account )
     *
     * @param account account for which all claims will be found and deleted
     */
    public void deleteClaims(Account account) {
        List<Gift> claimedGifts = giftRepository.findByClaimed(account);
        claimedGifts.forEach(gift -> gift.setClaimed(null));
        giftRepository.saveAll(claimedGifts);
    }

    /**
     * Fetch all application accounts
     *
     * @return List of all accounts
     */
    public List<Gift> findAll() {
        return giftRepository.findAll();
    }

    /**
     * Remove category , by finding all gifts with it, set it to null and save all
     *
     * @param category category to be removed from all gifts
     */
    public void removeCategory(Category category) {
        List<Gift> allByCategory = giftRepository.findAllByCategory(category);
        allByCategory.forEach(gift -> gift.setCategory(null));
        giftRepository.saveAll(allByCategory);
    }

    /**
     * Remove all search engines from gifts
     *
     * @param searchEngine search engine to be found, and removed from gifst
     */
    public void removeSearchEngine(SearchEngine searchEngine) {
        List<Gift> gifts = giftRepository.findByEngines(searchEngine);
        gifts.forEach(gift -> gift.getEngines().remove(searchEngine));
        giftRepository.saveAll(gifts);
    }
}
