package com.qprogramming.gifts.api.gift;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.*;

/**
 * Created by Khobar on 10.03.2017.
 */
@Controller
@RequestMapping("/api/gift")
public class GiftRestController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private AccountService accountService;
    private GiftService giftService;
    private SearchEngineService searchEngineService;
    private CategoryRepository categoryRepository;
    private MessagesService msgSrv;
    private FamilyService familyService;
    private MailService mailService;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService, CategoryRepository categoryRepository, MessagesService msgSrv, FamilyService familyService, MailService mailService) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
        this.categoryRepository = categoryRepository;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
        this.mailService = mailService;
    }

    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody GiftForm giftForm) {
        Gift newGift = new Gift();
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        if (canOperateOnUsernameGifts(giftForm.getUsername()) || StringUtils.isBlank(giftForm.getUsername())) {
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
        if (canOperateOnGift(gift)) {
            //TODO add edition to newsletter
            gift = updateGiftFromForm(giftForm, gift);
            return new ResponseEntity<>(gift, HttpStatus.OK);
        }
        return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    private boolean canOperateOnUsernameGifts(String username) {
        if (StringUtils.isNotBlank(username)) {
            Account giftOwner = accountService.findByUsername(username);
            if (giftOwner == null) {
                return false;
            }
            Family family = familyService.getFamily(giftOwner);
            return family == null
                    || (family.getAdmins().contains(Utils.getCurrentAccount()));
        }
        return false;
    }

    private boolean canOperateOnGift(Gift gift) {
        Account giftOwner = accountService.findById(gift.getUserId());
        if (giftOwner == null) {
            return false;
        }
        Family family = familyService.getFamily(giftOwner);
        return family == null
                || (family.getAdmins().contains(Utils.getCurrentAccount()))
                || giftOwner.equals(Utils.getCurrentAccount());
    }


    @RequestMapping(value = "/claim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity clameGift(@PathVariable(value = "giftID") String id) {
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
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.claim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale())).build();
    }

    @RequestMapping(value = "/unclaim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity unClameGift(@PathVariable(value = "giftID") String id) {
        Account account = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getClaimed(), account)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.unclaim.error")).build();
        }
        gift.setClaimed(null);
        giftService.update(gift);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.unclaim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build();
    }

    @RequestMapping(value = "/complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity completeGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.complete.error")).build();
        }
        gift.setStatus(GiftStatus.REALISED);
        giftService.update(gift);
        //TODO add complete event newsleter
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.complete.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build();
    }

    @RequestMapping(value = "/undo-complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity undoCompleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.complete.error")).build();
        }
        gift.setStatus(null);
        giftService.update(gift);
        //TODO add complete event newsleter
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.complete.undo.success")).build();
    }

    @RequestMapping(value = "/delete/{giftID}", method = RequestMethod.DELETE)
    public ResponseEntity deleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (gift == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.delete.error")).build();
        }
        giftService.delete(gift);
        //TODO add complete event newsleter
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.delete.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale())).build();
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
        if (Utils.getCurrentAccount() != null) {
            return new ResponseEntity<>(giftService.findAllByCurrentUser(), HttpStatus.OK);
        } else
            return ResponseEntity.ok(Collections.EMPTY_LIST);
    }

    @RequestMapping("/user/{usernameOrId}")
    public ResponseEntity getUserGifts(@PathVariable String usernameOrId) {
        Account account = accountService.findByUsername(usernameOrId);
        if (account == null) {
            account = accountService.findById(usernameOrId);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
        }
        //check if anonymous user can view user gifts
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            if (!account.getPublicList()) {
                return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.list.public.error")).build();
            }
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

    @RequestMapping(value = "/share", method = RequestMethod.POST)
    public ResponseEntity shareGiftList(List<String> emails) {
        Map<Category, List<Gift>> gifts = giftService.findAllByCurrentUser();
        List<Mail> emailList = new ArrayList<>();
        for (String email : emails) {
            Mail mail = new Mail();
            Account byEmail = accountService.findByEmail(email);
            mail.setMailTo(email);
            mail.setMailFrom(Utils.getCurrentAccount().getEmail());
            if (byEmail != null) {
                mail.setLocale(byEmail.getLanguage());
                mail.addToModel("name", byEmail.getFullname());
            }
            mail.addToModel("owner", Utils.getCurrentAccount().getFullname());
            mail.addToModel("gifts", gifts);
            emailList.add(mail);
        }
        try {
            mailService.shareGiftList(gifts, emailList);
        } catch (MessagingException e) {
            LOG.error("Error while sending emails {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(e.getMessage()).build();
        }
        return ResponseEntity.ok().build();
    }

}
