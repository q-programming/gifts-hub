package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.settings.SearchEngineService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Khobar on 10.03.2017.
 */
@Controller
@RequestMapping("/api/gift")
public class GiftRestController {

    private AccountService accountService;
    private GiftService giftService;
    private SearchEngineService searchEngineService;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody GiftForm giftForm) {
        Gift newGift = giftForm.createGift();
        //TODO add category later on
//        if (giftObj.has(Gift.CATEGORY)) {
//            giftObj.getString(Gift.CATEGORY);
//        }
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        newGift.setEngines(searchEngineService.getSearchEngines(giftForm.getSearchEngines()));
        Gift gift = giftService.create(newGift);
        return new ResponseEntity<>(gift, HttpStatus.CREATED);
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

    private String getIfExists(JSONObject jsonObject, String key) {
        return jsonObject.has(key) ? jsonObject.getString(key) : null;
    }
}
