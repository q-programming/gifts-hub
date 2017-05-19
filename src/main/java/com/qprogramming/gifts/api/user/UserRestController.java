package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.account.family.KidForm;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.login.token.TokenBasedAuthentication;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    public static final String PASSWORD_REGEXP = "^^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}$";
    private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    private AccountService accountService;
    private MessagesService msgSrv;
    private FamilyService familyService;
    private GiftService giftService;
    private MailService mailService;

    @Autowired
    public UserRestController(AccountService accountService, MessagesService msgSrv, FamilyService familyService, GiftService giftService, MailService mailService) {
        this.accountService = accountService;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
        this.giftService = giftService;
        this.mailService = mailService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@Valid @RequestBody RegisterForm userform) {
        if (accountService.findByEmail(userform.getEmail()) != null) {
            String message = msgSrv.getMessage("user.register.email.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResultData.ResultBuilder().error().message(message).build();
        }
        if (accountService.findByUsername(userform.getUsername()) != null) {
            String message = msgSrv.getMessage("user.register.username.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResultData.ResultBuilder().error().message(message).build();
        }
        if (!userform.getPassword().equals(userform.getConfirmpassword())) {
            return new ResultData.ResultBuilder().error().message(msgSrv.getMessage("user.register.password.nomatch")).build();
        }
        Pattern pattern = Pattern.compile(PASSWORD_REGEXP);
        Matcher matcher = pattern.matcher(userform.getPassword());
        if (!matcher.matches()) {
            return new ResultData.ResultBuilder().error().message(msgSrv.getMessage("user.register.password.tooweak")).build();
        }
        Account newAccount = userform.createAccount();
        newAccount = accountService.createLocalAccount(newAccount);
//        accountService.createAvatar(newAccount);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    @RequestMapping("/{id}/avatar")
    public ResponseEntity<?> userAvatar(@PathVariable(value = "id") String id) {
        Account account = accountService.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountService.getAccountAvatar(account));
    }

    @Transactional
    @RequestMapping("/avatar-upload")
    public ResponseEntity<?> uploadNewAvatar(@RequestBody String avatarStream) {
        Account account = Utils.getCurrentAccount();
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = Base64.decodeBase64(avatarStream);
        accountService.updateAvatar(account, data);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Returns user list, if passed param family is true, will return all accounts without family
     *
     * @param family if true , returned list will contain only accounts without family
     * @return
     */
    @RequestMapping("/users")
    public ResponseEntity<?> userList(@RequestParam(required = false) boolean family) {
        if (family) {
            return ResponseEntity.ok(accountService.findWithoutFamily());
        }
        return ResponseEntity.ok(accountService.findAll());
    }

    @RequestMapping("/families")
    public ResponseEntity<?> familyList() {
        return ResponseEntity.ok(familyService.findAll());
    }

    /**
     * Returns currently logged in user family ( or null if not found )
     *
     * @return {@link com.qprogramming.gifts.account.family.Family}
     */
    @RequestMapping("/family")
    public ResponseEntity<?> getUserFamily(@RequestParam(required = false) String username) {
        if (StringUtils.isNotBlank(username)) {
            Account account = accountService.findByUsername(username);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(familyService.getFamily(account));
        }
        return ResponseEntity.ok(familyService.getFamily(Utils.getCurrentAccount()));
    }

    @RequestMapping("/family-create")
    public ResponseEntity<?> createFamily(@RequestBody FamilyForm form) {
        Family family = familyService.getFamily(Utils.getCurrentAccount());
        if (family != null) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.exists.error")).build();
        }
        family = familyService.createFamily();
        family.getMembers().addAll(accountService.findByIds(form.getMembers()));
        family.getAdmins().addAll(accountService.findByIds(form.getAdmins()));
        if (StringUtils.isBlank(form.getName())) {
            form.setName(Utils.getCurrentAccount().getSurname());
        }
        family.setName(form.getName());
        return ResponseEntity.ok(familyService.update(family));
    }

    @RequestMapping("/family-update")
    public ResponseEntity<?> updateFamily(@RequestBody FamilyForm form) {
        Account currentAccount = Utils.getCurrentAccount();
        Family family = familyService.getFamily(currentAccount);
        if (family == null) {
            return ResponseEntity.notFound().build();
        }
        if (family.getAdmins().contains(currentAccount)) {
            Set<Account> members = new HashSet<>(accountService.findByIds(form.getMembers()));
            members.add(currentAccount);
            Set<Account> admins = new HashSet<>(accountService.findByIds(form.getAdmins()));
            admins.add(currentAccount);
            family.setMembers(members);
            family.setAdmins(admins);
            if (StringUtils.isBlank(form.getName())) {
                form.setName(Utils.getCurrentAccount().getSurname());
            }
            family.setName(form.getName());
            return ResponseEntity.ok(familyService.update(family));
        }
        return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    @Transactional
    @RequestMapping("/kid-add")
    public ResponseEntity<?> addKid(@RequestBody @Valid KidForm form) {
        if (accountService.findByUsername(form.getUsername()) != null) {
            String message = msgSrv.getMessage("user.register.username.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage(message)).build();
        }
        Account currentAccount = Utils.getCurrentAccount();
        Family family = familyService.getFamily(currentAccount);
        if (family == null) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.add.kid.error")).build();
        }
        if (!family.getAdmins().contains(currentAccount)) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.admin.error")).build();
        }
        Account kidAccount = form.createAccount();
        kidAccount = accountService.createKidAccount(kidAccount);
        family.getMembers().add(kidAccount);
        familyService.update(family);
        if (StringUtils.isNotBlank(form.getAvatar())) {
            byte[] data = Base64.decodeBase64(form.getAvatar());
            accountService.updateAvatar(kidAccount, data);
        }
        return ResponseEntity.ok(kidAccount);
    }

    @Transactional
    @RequestMapping("/kid-update")
    public ResponseEntity<?> updateKid(@RequestBody @Valid KidForm form) {
        Account currentAccount = Utils.getCurrentAccount();
        Family family = familyService.getFamily(currentAccount);
        if (family == null) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.add.kid.error")).build();
        }
        if (!family.getAdmins().contains(currentAccount)) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.admin.error")).build();
        }
        Account kidAccount = accountService.findById(form.getId());
        if (kidAccount == null) {
            return ResponseEntity.notFound().build();
        }
        if (StringUtils.isNotBlank(form.getName())) {
            kidAccount.setName(form.getName());
        }
        if (StringUtils.isNotBlank(form.getSurname())) {
            kidAccount.setSurname(form.getSurname());
        }
        kidAccount.setPublicList(form.getPublicList());
        accountService.update(kidAccount);
        if (StringUtils.isNotBlank(form.getAvatar())) {
            byte[] data = Base64.decodeBase64(form.getAvatar());
            accountService.updateAvatar(kidAccount, data);
        }
        return ResponseEntity.ok(kidAccount);
    }


    @RequestMapping(value = "/validate-email", method = RequestMethod.POST)
    public ResponseEntity validateEmail(@RequestBody String email) {
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return ResponseEntity.ok(new ResultData.ResultBuilder().ok().build());
        }
        return ResponseEntity.ok(new ResultData.ResultBuilder().error().message("User exists").build());
    }

    @RequestMapping(value = "/validate-username", method = RequestMethod.POST)
    public ResponseEntity validateUsername(@RequestBody String username) {
        Account acc = accountService.findByUsername(username);
        if (acc == null) {
            return ResponseEntity.ok(new ResultData.ResultBuilder().ok().build());
        }
        return ResponseEntity.ok(new ResultData.ResultBuilder().error().message("User exists").build());
    }

    @RequestMapping(value = "/tour-complete", method = RequestMethod.POST)
    public ResponseEntity completeTour() {
        Account account = accountService.findById(Utils.getCurrentAccountId());
        if (account == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setTourComplete(true);
        account.setTourComplete(true);
        accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/tour-reset", method = RequestMethod.POST)
    public ResponseEntity resetTour() {
        Account account = accountService.findById(Utils.getCurrentAccountId());
        if (account == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setTourComplete(false);
        account.setTourComplete(false);
        accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity changeSettings(@RequestBody String jsonObj) {
        JSONObject object = new JSONObject(jsonObj);
        Account currentAccount = Utils.getCurrentAccount();
        Account account = accountService.findById(object.getString("id"));
        if (account == null || !account.equals(currentAccount)) {
            return ResponseEntity.notFound().build();
        }
        if (object.has("language")) {
            account.setLanguage(object.getString("language"));
        }
        if (object.has("publicList")) {
            account.setPublicList(object.getBoolean("publicList"));
        }
        accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "/delete/{userID}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAccount(@PathVariable(value = "userID") String id) {
        Account account = accountService.findById(id);
        if (account == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        String message;
        if (!account.equals(Utils.getCurrentAccount())) {
            if (!account.getType().equals(AccountType.KID)) {
                return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.delete.error")).build();
            }
            //if not deleting his account
            Family family = familyService.getFamily(account);
            if (family == null || !family.getAdmins().contains(Utils.getCurrentAccount())) {
                return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.delete.error")).build();
            }
            message = msgSrv.getMessage("user.family.delete.kid.success");
        } else {
            message = msgSrv.getMessage("user.delete.success");
        }
        giftService.deleteClaims(account);
        giftService.deleteUserGifts(account);
        accountService.delete(account);
        //TODO add complete event newsleter
        return new ResultData.ResultBuilder().ok().message(message).build();
    }


    @RequestMapping
    public Account user(@RequestParam(required = false) String identification, Principal user) {
        if (StringUtils.isNotBlank(identification)) {
            return getAccountByUsernameOrId(identification);
        }
        if (user != null && user instanceof UsernamePasswordAuthenticationToken) {
            return (Account) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
        } else if (user != null && user instanceof TokenBasedAuthentication) {
            return (Account) ((TokenBasedAuthentication) user).getPrincipal();
        }
        return null;
    }

    /**
     * Return list of all admins in application.
     *
     * @return {@link List}<{@link Account}>admins or forbidden if current account is empty or is not admin
     */
    @RequestMapping(value = "admins", method = RequestMethod.GET)
    public ResponseEntity admins() {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(accountService.findAdmins());
    }

    private Account getAccountByUsernameOrId(@RequestParam(required = false) String identification) {
        Account account = accountService.findByUsername(identification);
        if (account == null) {
            account = accountService.findById(identification);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return account != null && account.getPublicList() ? account : null;
        }
        return account;
    }

    /**
     * Shares public link with email recipients. Passed emails should be ; delimited. Only valid emails will be used to send emails to
     *
     * @param emails ; delimited email address
     * @return
     */
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    public ResponseEntity shareGiftList(@RequestBody String emails) {
        if (!Utils.getCurrentAccount().getPublicList()) {
            return new ResultData.ResultBuilder().badReqest().error().build();
        }
        List<Mail> mailList = new ArrayList<>();
        List<String> emailLists = Arrays.stream(emails.split(";")).filter(Utils::validateEmail).collect(Collectors.toList());
        for (String email : emailLists) {
            Mail mail = new Mail();
            Account byEmail = accountService.findByEmail(email);
            mail.setMailTo(email);
            mail.setMailFrom(Utils.getCurrentAccount().getEmail());
            if (byEmail != null) {
                mail.setLocale(byEmail.getLanguage());
                mail.addToModel("name", byEmail.getFullname());
            }
            mail.addToModel("owner", Utils.getCurrentAccount().getFullname());
            mailList.add(mail);
        }
        try {
            mailService.shareGiftList(mailList);
        } catch (MessagingException e) {
            LOG.error("Error while sending emailLists {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(e.getMessage()).build();
        }
        String message = msgSrv.getMessage("gift.share.success", new Object[]{StringUtils.join(emailLists, ", ")}, "", Utils.getCurrentLocale());
        return new ResultData.ResultBuilder().ok().message(message).build();
    }
}
