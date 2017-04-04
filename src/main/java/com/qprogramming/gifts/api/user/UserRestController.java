package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    public static final String EXISTS = "exists";
    public static final String PASSWORD_REGEXP = "^^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}$";
    private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    private AccountService accountService;
    private MessagesService msgSrv;
    private FamilyService familyService;

    @Autowired
    public UserRestController(AccountService accountService, MessagesService msgSrv, FamilyService familyService) {
        this.accountService = accountService;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@Valid @RequestBody RegisterForm userform) {
        if (accountService.findByEmail(userform.getEmail()) != null) {
            String message = msgSrv.getMessage("user.register.email.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResultData.ResultBuilder().error().message(message).build();
        }
        if (accountService.findByUsername(userform.getUsername()) != null) {
            String message = msgSrv.getMessage("user.register.username.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResultData.ResultBuilder().error().message(msgSrv.getMessage(message)).build();
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
        newAccount = accountService.create(newAccount);
        accountService.createAvatar(newAccount);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping("/{id}/avatar")
    public ResponseEntity<?> userAvatar(@PathVariable(value = "id") String id) {
        Account account = accountService.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountService.getAccountAvatar(account));
    }

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

    @RequestMapping("/users")
    public ResponseEntity<?> userList(@RequestParam(required = false) boolean family) {
        if (family) {
            return ResponseEntity.ok(accountService.findWithoutFamily());
        }
        return ResponseEntity.ok(accountService.findAll());
    }

    /**
     * Returns currently logged in user family ( or null if not found )
     *
     * @return {@link com.qprogramming.gifts.account.family.Family}
     */
    @RequestMapping("/family")
    public ResponseEntity<?> getUserFamily() {
        return ResponseEntity.ok(familyService.getFamily(Utils.getCurrentAccount()));
    }

    @RequestMapping("/family-create")
    public ResponseEntity<?> createFamily(@RequestBody FamilyForm form) {
        Family family = familyService.getFamily(Utils.getCurrentAccount());
        if (family != null) {
            return new ResultData.ResultBuilder().error().message(msgSrv.getMessage("user.family.exists.error")).build();
        }
        family = familyService.createFamily();
        family.getMembers().addAll(accountService.findByIds(form.getMembers()));
        family.getAdmins().addAll(accountService.findByIds(form.getAdmins()));
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
            return ResponseEntity.ok(familyService.update(family));
        }
        return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.admin.error")).build();
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


    @RequestMapping(value = "/language", method = RequestMethod.POST)
    public ResponseEntity changeLanguage(@RequestBody String jsonObj) {
        JSONObject object = new JSONObject(jsonObj);
        Account currentAccount = Utils.getCurrentAccount();
        Account account = accountService.findById(object.getString("id"));
        if (account == null || !account.equals(currentAccount)) {
            return ResponseEntity.notFound().build();
        }
        account.setLanguage(object.getString("language"));
        accountService.update(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping
    public Account user(Principal user) {
        if (user != null && user instanceof UsernamePasswordAuthenticationToken) {
            return (Account) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
        }
        return null;
    }

}
