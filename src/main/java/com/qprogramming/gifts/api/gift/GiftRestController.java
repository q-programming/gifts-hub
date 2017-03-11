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
    private Class<Gift> giftClass;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService) {
        this.accountService = accountService;
        this.giftService = giftService;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody String giftString) {
        JSONObject giftObj = new JSONObject(giftString);
        Gift newGift = new Gift();
        newGift.setName(giftObj.getString("name"));
        if (giftObj.has("link")) {
            newGift.setLink(giftObj.getString("link"));
        }
        //TODO add category later on
        if (giftObj.has("category")) {
            giftObj.getString("category");
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
}
