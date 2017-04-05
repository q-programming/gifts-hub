package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.GiftStatus;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryRepository;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

/**
 * Created by Khobar on 10.03.2017.
 */
@Controller
@RequestMapping("/api/gift")
public class GiftRestController {

    private AccountService accountService;
    private GiftService giftService;
    private SearchEngineService searchEngineService;
    private CategoryRepository categoryRepository;
    private MessagesService msgSrv;
    private FamilyService familyService;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService, CategoryRepository categoryRepository, MessagesService msgSrv, FamilyService familyService) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
        this.categoryRepository = categoryRepository;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody GiftForm giftForm) {
        Gift newGift = new Gift();
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        if (canOperateOnUsernameGifts(giftForm) || StringUtils.isBlank(giftForm.getUsername())) {
            Gift gift = updateGiftFromForm(giftForm, newGift);
            return new ResponseEntity<>(gift, HttpStatus.CREATED);
        }
        return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    @RequestMapping("/edit")
    public ResponseEntity editGift(@RequestBody GiftForm giftForm) {
        Gift gift = giftService.findById(giftForm.getId());
        if (gift == null) {
            return ResponseEntity.notFound().build();
        }
        if (canOperateOnUsernameGifts(giftForm) || gift.getUserId().equals(Utils.getCurrentAccount().getId())) {
            //TODO add edition to newsletter
            gift = updateGiftFromForm(giftForm, gift);
            return new ResponseEntity<>(gift, HttpStatus.OK);
        }
        return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    private boolean canOperateOnUsernameGifts(GiftForm giftForm) {
        if (StringUtils.isNotBlank(giftForm.getUsername())) {
            Account giftOwner = accountService.findByUsername(giftForm.getUsername());
            if (giftOwner == null) {
                return false;
            }
            Family family = familyService.getFamily(giftOwner);
            return family == null
                    || (family.getAdmins().contains(Utils.getCurrentAccount()));
        }
        return false;
    }


    @RequestMapping("/claim")
    public ResponseEntity clameGift(@RequestParam(value = "gift") String id) {
        Account account = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (Objects.equals(gift.getUserId(), account.getId())) {
            return ResponseEntity.badRequest().body(msgSrv.getMessage("gift.claim.same"));
        }
        gift.setClaimed(account);
        giftService.update(gift);
        return new ResponseEntity<>(new ResultData.ResultBuilder()
                .ok().message(msgSrv.getMessage("gift.claim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build(), HttpStatus.OK);
    }

    @RequestMapping("/unclaim")
    public ResponseEntity unClameGift(@RequestParam(value = "gift") String id) {
        Account account = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getClaimed(), account)) {
            return ResponseEntity.badRequest().body(msgSrv.getMessage("gift.unclaim.error"));
        }
        gift.setClaimed(null);
        giftService.update(gift);
        return new ResponseEntity<>(new ResultData.ResultBuilder()
                .ok().message(msgSrv.getMessage("gift.unclaim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build(), HttpStatus.OK);
    }

    @RequestMapping("/complete")
    public ResponseEntity completeGift(@RequestParam(value = "gift") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getUserId(), Utils.getCurrentAccount().getId())) {
            return ResponseEntity.badRequest().body(msgSrv.getMessage("gift.complete.error"));
        }
        gift.setStatus(GiftStatus.REALISED);
        giftService.update(gift);
        //TODO add complete event newsleter
        return new ResponseEntity<>(new ResultData.ResultBuilder()
                .ok().message(msgSrv.getMessage("gift.complete.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build(), HttpStatus.OK);
    }

    @RequestMapping("/undo-complete")
    public ResponseEntity undoCompleteGift(@RequestParam(value = "gift") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getUserId(), Utils.getCurrentAccount().getId())) {
            return ResponseEntity.badRequest().body(msgSrv.getMessage("gift.complete.error"));
        }
        gift.setStatus(null);
        giftService.update(gift);
        //TODO add complete event newsleter
        return new ResponseEntity<>(new ResultData.ResultBuilder()
                .ok().message(msgSrv.getMessage("gift.complete.undo.success"))
                .build(), HttpStatus.OK);
    }


    /**
     * Update gift with data from {@link GiftForm}
     *
     * @param giftForm form from which data will updated
     * @param gift     updated  created gift
     * @return updated {@link Gift}
     */
    private Gift updateGiftFromForm(GiftForm giftForm, Gift gift) {
        gift.setName(giftForm.getName());
        gift.setDescription(giftForm.getDescription());
        gift.setLink(giftForm.getLink());
        if (StringUtils.isNotBlank(giftForm.getCategory())) {
            String name = giftForm.getCategory();
            Category category = categoryRepository.findByName(name);
            if (category == null) {
                category = categoryRepository.save(new Category(name));
            }
            gift.setCategory(category);
        }
        gift.setEngines(searchEngineService.getSearchEngines(giftForm.getSearchEngines()));
        //update or create
        if (gift.getId() == null) {
            if (StringUtils.isNotBlank(giftForm.getUsername())) {
                Account account = accountService.findByUsername(giftForm.getUsername());
                gift.setUserId(account.getId());
            } else {
                Account currentAccount = Utils.getCurrentAccount();
                gift.setUserId(currentAccount.getId());
            }
            return giftService.create(gift);
        } else {
            return giftService.update(gift);
        }
    }

    @RequestMapping("/mine")
    public ResponseEntity getUserGifts() {
        return new ResponseEntity<>(giftService.findAllByCurrentUser(), HttpStatus.OK);
    }

    @RequestMapping("/user/{username}")
    public ResponseEntity getUserGifts(@PathVariable String username) {
        Account account = accountService.findByUsername(username);
        if (account == null) {
            return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(giftService.findAllByUser(account.getId()), HttpStatus.OK);
    }

    @RequestMapping("/categories")
    public ResponseEntity getCategories(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(categoryRepository.findAll());
        } else {
            return ResponseEntity.ok(categoryRepository.findByNameContainingIgnoreCase(term));
        }
    }
}
