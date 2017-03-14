package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
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

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService) {
        this.accountService = accountService;
        this.giftService = giftService;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody String giftString) {
        JSONObject giftObj = new JSONObject(giftString);
        if(!giftObj.has(Gift.NAME)){
            return new ResponseEntity<>("Name field is required",HttpStatus.BAD_REQUEST);
        }
        Gift newGift = new Gift();
        newGift.setName(giftObj.getString(Gift.NAME));
        newGift.setDescription(getIfExists(giftObj, Gift.DESCRIPTION));
        newGift.setLink(getIfExists(giftObj, Gift.LINK));
        //TODO add category later on
        if (giftObj.has(Gift.CATEGORY)) {
            giftObj.getString(Gift.CATEGORY);
        }
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
