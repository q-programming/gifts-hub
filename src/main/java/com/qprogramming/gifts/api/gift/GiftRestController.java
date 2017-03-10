package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
        newGift.setLink(giftObj.getString("link"));
        //TODO add category later on
        giftObj.getString("category");
        Gift gift = giftService.create(newGift);
        return new ResponseEntity<>(gift, HttpStatus.CREATED);
    }
}
