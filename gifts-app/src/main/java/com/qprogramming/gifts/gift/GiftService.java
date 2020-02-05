package com.qprogramming.gifts.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.image.GiftImage;
import com.qprogramming.gifts.gift.image.GiftImageRepository;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.settings.Settings.APP_GIFT_AGE;
import static com.qprogramming.gifts.support.Utils.decodeTypeFromBytes;
import static com.qprogramming.gifts.support.Utils.not;
import static java.util.stream.Collectors.groupingBy;

@Service
public class GiftService {

    private GiftRepository giftRepository;
    private PropertyService propertyService;
    private GiftImageRepository imageRepository;

    public GiftService(GiftRepository giftRepository, PropertyService propertyService, GiftImageRepository imageRepository) {
        this.giftRepository = giftRepository;
        this.propertyService = propertyService;
        this.imageRepository = imageRepository;
    }

    /**
     * Converts gifts list to TreeMap based on their's category
     * After map was build each list is sorted
     *
     * @param gifts list of gifts to put into TreeMap
     * @return TreeMap with gifts sorted by categories ( based on priorities)
     */
    public Map<Category, List<Gift>> toGiftTreeMap(List<Gift> gifts, boolean simple) {
        Map<Category, List<Gift>> result = Utils.toGiftTreeMap(gifts);
        if (simple) {
            result.values().forEach(GiftComparator::simpleSortGiftList);
        } else {
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
    public Map<Category, List<Gift>> findAllByCurrentUser(boolean realised) {
        return findAllByUser(Utils.getCurrentAccountId(), realised);
    }

    /**
     * Fetch currently logged in user gifts. Remove claimed status and filter out hidden gifts
     *
     * @return List of all users gifts
     */
    private List<Gift> getCurrentUserGifts(boolean realised) {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        String id = Utils.getCurrentAccountId();
        List<Gift> giftList = realised ? giftRepository.findByUserIdAndRealisedIsNotNullOrderByRealisedDesc(id) : giftRepository.findByUserIdAndRealisedIsNullOrderByCreatedDesc(id);
        return giftList.stream().filter(not(Gift::isHidden)).peek(gift -> {
            setGiftStatus(gift, giftAge);
            gift.setClaimed(null);//remove claimed as current user shouldn't see it
        }).collect(Collectors.toList());
    }

    /**
     * Fetch all gifts for account with id
     * If Current account is empty ( annonymous user  viewing public list ) , filter out all hidden gifts
     *
     * @param id Account id for which gifts will be fetched
     * @return List of all Gifts for Account with ID
     */
    private List<Gift> getUserGifts(String id, boolean realised) {
        int giftAge = Integer.valueOf(propertyService.getProperty(APP_GIFT_AGE));
        List<Gift> giftList = realised ? giftRepository.findByUserIdAndRealisedIsNotNullOrderByRealisedDesc(id) : giftRepository.findByUserIdAndRealisedIsNullOrderByCreatedDesc(id);
        List<Gift> gifts = giftList.stream().peek(gift -> setGiftStatus(gift, giftAge)).collect(Collectors.toList());
        if (Utils.getCurrentAccount() == null) {
            gifts = gifts
                    .stream()
                    .filter(not(Gift::isHidden))
                    .peek(gift -> gift.setClaimed(null))
                    .collect(Collectors.toList());
        }
        return gifts;
    }


    /**
     * Returns a tree map of Category,GiftList for user
     *
     * @param id id of user for which gift tree map will be returned
     * @return Map of Category - List of gifts for it
     */
    public Map<Category, List<Gift>> findAllByUser(String id, boolean realised) {
        List<Gift> giftList = id.equals(Utils.getCurrentAccountId()) ?
                getCurrentUserGifts(realised) : getUserGifts(id, realised);
        boolean simple = id.equals(Utils.getCurrentAccountId());
        return toGiftTreeMap(giftList, simple);
    }

    /**
     * Count all gifts for user with ID
     *
     * @param id account id to be checked
     * @return count of all gifts for account
     */
    public int countAllByAccountId(String id) {
        return (int) giftRepository.findByUserIdAndRealisedIsNullOrderByCreatedDesc(id)
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
     * Updates image on gift. {@link Gift#getImageData()} will be evaluated and base on it's content ( or it's lack ) update will happen
     *
     * @param gift gift to be updated
     */
    public void updateGiftImage(Gift gift, String imageData) {
        GiftImage image = gift.getImage();
        if (StringUtils.isNotBlank(imageData)) {
            if (image == null) {
                image = new GiftImage();
            }
            image.setImage(Base64.decodeBase64(imageData));
            image.setType(decodeTypeFromBytes(image.getImage()));
            gift.setImage(imageRepository.save(image));
            gift.setHasImage(true);
        } else {
            if (image != null) {
                gift.setImage(null);
                gift.setHasImage(false);
                imageRepository.delete(image);
            }
        }
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
            } else {
                gift.setStatus(null);
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
        List<Gift> userGifts = giftRepository.findByUserIdAndRealisedIsNullOrderByCreatedDesc(account.getId());
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
     * Finds all gifts that were claimed by current account
     */
    public Map<String, List<Gift>> findAllClaimedByCurrentUser() {
        List<Gift> gifts = giftRepository.findAllByClaimedAndStatusIsNull(Utils.getCurrentAccount());
        return gifts.stream().collect(groupingBy(Gift::getUserId));
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

    public void mergeCategories(Category newCategory, List<Category> categoriesList) {
        List<Gift> allInOldCategories = giftRepository.findAllByCategoryIsIn(categoriesList);
        allInOldCategories.forEach(gift -> gift.setCategory(newCategory));
        giftRepository.saveAll(allInOldCategories);
    }
}
