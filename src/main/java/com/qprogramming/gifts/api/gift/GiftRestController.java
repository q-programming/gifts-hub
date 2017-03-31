package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
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

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService, CategoryRepository categoryRepository, MessagesService msgSrv) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
        this.categoryRepository = categoryRepository;
        this.msgSrv = msgSrv;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody GiftForm giftForm) {
        Gift newGift = giftForm.createGift();
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        Gift gift = createGiftFromForm(giftForm, newGift);
        return new ResponseEntity<>(gift, HttpStatus.CREATED);
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

    //TODO move to service
    private Gift createGiftFromForm(@RequestBody GiftForm giftForm, Gift newGift) {
        if (StringUtils.isNotBlank(giftForm.getCategory())) {
            String name = giftForm.getCategory();
            Category category = categoryRepository.findByName(name);
            if (category == null) {
                category = categoryRepository.save(new Category(name));
            }
            newGift.setCategory(category);
        }
        newGift.setEngines(searchEngineService.getSearchEngines(giftForm.getSearchEngines()));
        return giftService.create(newGift);
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
