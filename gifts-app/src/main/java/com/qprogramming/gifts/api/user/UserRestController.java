package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyForm;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.account.family.KidForm;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.login.token.TokenBasedAuthentication;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class UserRestController {

    public static final String PASSWORD_REGEXP = "^^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}$";
    public static final String USERNAME_REGEXP = "^[a-zA-Z0-9_]+$";
    public static final String NEWSLETTER = "notifications";
    public static final String PUBLIC_LIST = "publicList";
    public static final String LANGUAGE = "language";
    private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    private AccountService accountService;
    private MessagesService msgSrv;
    private FamilyService familyService;
    private GiftService giftService;
    private MailService mailService;
    private AppEventService eventService;
    private LogoutHandler logoutHandler;

    @Autowired
    public UserRestController(AccountService accountService, MessagesService msgSrv, FamilyService familyService, GiftService giftService, MailService mailService, AppEventService eventService, LogoutHandler logoutHandler) {
        this.accountService = accountService;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
        this.giftService = giftService;
        this.mailService = mailService;
        this.eventService = eventService;
        this.logoutHandler = logoutHandler;
    }

    /**
     * Returns currently logged in user as {@link Account}
     *
     * @return currently logged in user
     */
    @RequestMapping("/whoami")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Account user() {
        return (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterForm userform) {
        if (accountService.findByEmail(userform.getEmail()).isPresent()) {
            String message = msgSrv.getMessage("user.register.email.exists") + " " + msgSrv.getMessage("user.register.alreadyexists");
            return new ResponseEntity<>("email", HttpStatus.CONFLICT);
        }
        Pattern pattern = Pattern.compile(USERNAME_REGEXP);
        Matcher matcher = pattern.matcher(userform.getUsername());
        if (!matcher.matches()) {
            return new ResponseEntity<>("bad_username", HttpStatus.CONFLICT);
        }

        if (accountService.findByUsername(userform.getUsername()).isPresent()) {
            return new ResponseEntity<>("username", HttpStatus.CONFLICT);
        }
        if (!userform.getPassword().equals(userform.getConfirmpassword())) {
            return new ResponseEntity<>("passwords", HttpStatus.CONFLICT);
        }
        if (userform.getPassword().length() < 8) {
            return new ResponseEntity<>("weak", HttpStatus.CONFLICT);
        }
        Account newAccount = userform.createAccount();
        newAccount = accountService.createLocalAccount(newAccount);
//        accountService.createAvatar(newAccount);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    @RequestMapping("/{username}/avatar")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userAvatar(@PathVariable(value = "username") String username) {
        Optional<Account> optionalAccount = accountService.findByUsername(username);
        if (!optionalAccount.isPresent()) {
            LOG.error("Unable to find account with username {}", username);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountService.getAccountAvatar(optionalAccount.get()));
    }

    @Transactional
    @RequestMapping("/avatar-upload")
    @PreAuthorize("hasRole('ROLE_USER')")
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
     * @param noFamily if true , returned list will contain only accounts without family
     * @param users    if true, only users will be retured ( no KID accounts ) and gifts count's won't be added to response
     * @return list of application accounts
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<?> userList(@RequestParam(required = false) boolean noFamily, @RequestParam(required = false) boolean users) {
        Set<Account> list;
        if (noFamily) {
            list = new LinkedHashSet<>(accountService.findWithoutFamily());
            addGiftCounts(list);
        } else {
            if (users) {
                list = new LinkedHashSet<>(accountService.findUsers());
            } else {
                list = new LinkedHashSet<>(accountService.findAll());
                addGiftCounts(list);
            }
        }
        return ResponseEntity.ok(list);

    }

    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userSearchList(@RequestParam(required = false) String username) {
        Account account = Utils.getCurrentAccount();
        if (StringUtils.isNotBlank(username)) {
            Optional<Account> optionalAccount = accountService.findByUsername(username);
            if (!optionalAccount.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            account = optionalAccount.get();
        }
        Set<Account> list = accountService.findAllSortByFamily(account);
        return ResponseEntity.ok(list);
    }


    @RequestMapping("/families")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> familyList() {
        List<Family> families = familyService.findAll();
        families.forEach(family -> {
            family.setMembers(new TreeSet<>(family.getMembers()));
            addGiftCounts(family.getMembers());
            markAdmins(family);
        });
        return ResponseEntity.ok(families);
    }

    private void addGiftCounts(Set<Account> list) {
        list.forEach(account -> {
            account.setGiftsCount(giftService.countAllByUser(account.getId()));
        });
    }

    private void markAdmins(Family family) {
        family.getMembers().forEach(account -> {
            if (family.getAdmins().contains(account)) {
                account.setFamilyAdmin(true);
            }
        });
    }

    /**
     * Returns currently logged in user family ( or null if not found )
     *
     * @return {@link com.qprogramming.gifts.account.family.Family}
     */
    @RequestMapping(value = "/family", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getUserFamily(@RequestParam(required = false) String username) {
        if (StringUtils.isNotBlank(username)) {
            Optional<Account> account = accountService.findByUsername(username);
            if (!account.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(familyService.getFamily(account.get()).get());
        }
        return ResponseEntity.ok(familyService.getFamily(Utils.getCurrentAccount()));
    }

    @Transactional
    @RequestMapping(value = "/family-create", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createFamily(@RequestBody FamilyForm form) {
        Optional<Family> optionalFamily = familyService.getFamily(Utils.getCurrentAccount());
        if (optionalFamily.isPresent()) {
            return new ResultData.ResultBuilder().badReqest().message(msgSrv.getMessage("user.family.exists.error")).build();
        }
        Family family = familyService.createFamily();
        if (StringUtils.isBlank(form.getName())) {
            form.setName(Utils.getCurrentAccount().getSurname());
        }
        family.setName(form.getName());
        family = familyService.update(family);
        Set<Account> members = accountService.findByEmailsOrUsernames(form.getMembers());
        try {
            sendInvites(members, family, AccountEventType.FAMILY_MEMEBER);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        //family.getAdmins().addAll(accountService.findByIds(form.getAdmins()));
        HashMap<String, String> model = new HashMap<>();
        if (members.size() > 0) {
            model.put("result", "invites");
        } else {
            model.put("result", "ok");
        }
        return ResponseEntity.ok(model);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/family-update", method = RequestMethod.PUT)
    public ResponseEntity<?> updateFamily(@RequestBody FamilyForm form) {
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Family> optionalFamily = familyService.getFamily(currentAccount);
        if (!optionalFamily.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Family family = optionalFamily.get();
        if (family.getAdmins().contains(currentAccount)) {
            //set members
            Set<Account> formMembers = accountService.findByEmailsOrUsernames(form.getMembers());
            Set<Account> membersToInvite = formMembers.stream().filter(account -> !family.getMembers().contains(account)).collect(Collectors.toSet());
            formMembers.removeAll(membersToInvite);
            family.setMembers(formMembers);
            //set admins
            Set<Account> formAdmins = accountService.findByEmailsOrUsernames(form.getAdmins());
            Set<Account> adminsToInvite = formAdmins.stream().filter(account -> !family.getAdmins().contains(account)).collect(Collectors.toSet());
            formAdmins.removeAll(adminsToInvite);
            family.setAdmins(formAdmins);
            //send invites, remove all double members if are in admin list
            try {
                membersToInvite.removeAll(adminsToInvite);
                sendInvites(membersToInvite, family, AccountEventType.FAMILY_MEMEBER);
                sendInvites(adminsToInvite, family, AccountEventType.FAMILY_ADMIN);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if (StringUtils.isBlank(form.getName())) {
                form.setName(Utils.getCurrentAccount().getSurname());
            }
            family.setName(form.getName());
            HashMap<String, String> model = new HashMap<>();
            if (membersToInvite.size() > 0 || adminsToInvite.size() > 0) {
                model.put("result", "invites");
            } else {
                model.put("result", "ok");
            }
            return ResponseEntity.ok(model);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/family-leave", method = RequestMethod.PUT)
    public ResponseEntity<?> leaveFamily() {
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Family> optionalFamily = familyService.getFamily(currentAccount);
        if (!optionalFamily.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Family family = familyService.removeFromFamily(currentAccount, optionalFamily.get());
        return ResponseEntity.ok(family);
    }


    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/confirm")
    public ResponseEntity confirmOperation(@RequestBody String token) {
        UUID uuid = UUID.fromString(token);
        Optional<AccountEvent> eventOptional = accountService.findEvent(token);
        if (!eventOptional.isPresent()) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        DateTime expireDate = date.plusDays(7);
        if (new DateTime().isAfter(expireDate)) {
            accountService.removeEvent(event);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
        }
        if (!event.getType().equals(AccountEventType.ACCOUNT_CONFIRM) && !event.getAccount().equals(Utils.getCurrentAccount())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Family> optionalFamily;
        HashMap<String, String> model = new HashMap<>();
        switch (event.getType()) {
            case FAMILY_MEMEBER:
                optionalFamily = familyService.getFamily(Utils.getCurrentAccount());
                if (optionalFamily.isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("family_exists");
                }
                familyService.addAccountToFamily(Utils.getCurrentAccount(), event.getFamily());
                accountService.eventConfirmed(event);
                model.put("result", "family_member");
                return ResponseEntity.ok(model);
            case FAMILY_ADMIN:
                optionalFamily = familyService.getFamily(Utils.getCurrentAccount());
                if (optionalFamily.isPresent() && optionalFamily.get() != event.getFamily()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("family_exists");
                }
                Family family = familyService.addAccountToFamily(Utils.getCurrentAccount(), event.getFamily());
                familyService.addAccountToFamilyAdmins(Utils.getCurrentAccount(), family);
                accountService.eventConfirmed(event);
                model.put("result", "family_admin");
                return ResponseEntity.ok(model);
            case FAMILY_REMOVE:
                //TODO not used
                break;
            case ACCOUNT_CONFIRM:

                break;
        }
        return new ResultData.ResultBuilder().badReqest().build();
    }


    private ResponseEntity familyExistsResponse(Family family) {
        return new ResultData.ResultBuilder()
                .badReqest()
                .message(msgSrv.getMessage("user.confirm.family.exists", new Object[]{family.getName()}, "", Utils.getCurrentLocale()))
                .build();
    }

    private void sendInvites(Set<Account> members, Family family, AccountEventType type) throws MessagingException {
        List<Account> list = new ArrayList<>(members);
        sendInvites(list, family, type);
    }

    private void sendInvites(List<Account> members, Family family, AccountEventType type) throws MessagingException {
        for (Account account : members) {
            if (AccountType.KID.equals(account.getType())) {
                familyService.addAccountToFamily(account, family);
            } else if (AccountType.TEMP.equals(account.getType())) {
                Mail mail = Utils.createMail(account, Utils.getCurrentAccount());
                mailService.sendInvite(mail, family.getName());
            } else {
                AccountEvent event = familyService.inviteAccount(account, family, type);
                Mail mail = Utils.createMail(account, Utils.getCurrentAccount());
                mailService.sendConfirmMail(mail, event);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/kid-add")
    public ResponseEntity<?> addKid(@RequestBody @Valid KidForm form) {
        Optional<Account> optionalAccount = accountService.findByUsername(form.getUsername());
        if (optionalAccount.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username");
        }
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Family> optionalFamily = familyService.getFamily(currentAccount);
        if (!optionalFamily.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("family");
        }
        Family family = optionalFamily.get();
        if (!family.getAdmins().contains(currentAccount)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("family_admin");
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
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/kid-update")
    public ResponseEntity<?> updateKid(@RequestBody @Valid KidForm form) {
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Family> optionalFamily = familyService.getFamily(currentAccount);
        if (!optionalFamily.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("family");
        }
        Family family = optionalFamily.get();
        if (!family.getAdmins().contains(currentAccount)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("family_admin");
        }
        Account kidAccount;
        try {
            kidAccount = accountService.findById(form.getId());
        } catch (AccountNotFoundException e) {
            LOG.error("Unable to find KID account with id {}", form.getId());
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
        Optional<Account> acc = accountService.findByEmail(email);
        if (!acc.isPresent()) {
            return ResponseEntity.ok(new ResultData.ResultBuilder().ok().build());
        }
        return ResponseEntity.ok(new ResultData.ResultBuilder().error().message("User exists").build());
    }

    @RequestMapping(value = "/validate-username", method = RequestMethod.POST)
    public ResponseEntity validateUsername(@RequestBody String username) {
        Optional<Account> optionalAccount = accountService.findByUsername(username);
        if (!optionalAccount.isPresent()) {
            return ResponseEntity.ok(new ResultData.ResultBuilder().ok().build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/tour-complete", method = RequestMethod.POST)
    public ResponseEntity completeTour() {
        Account account;
        try {
            account = accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (account == null) {
        }
        Utils.getCurrentAccount().setTourComplete(true);
        account.setTourComplete(true);
        accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/tour-reset", method = RequestMethod.POST)
    public ResponseEntity resetTour() {
        Account account = null;
        try {
            account = accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setTourComplete(false);
        account.setTourComplete(false);
        account = accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/changelog", method = RequestMethod.POST)
    public ResponseEntity changelogRead() {
        Account account;
        try {
            account = accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setSeenChangelog(true);
        account.setSeenChangelog(true);
        account = accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity changeSettings(@RequestBody AccountSettings accountSettings) {
        //TODO rewrite ?
        Account account;
        try {
            account = accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (StringUtils.isNotBlank(accountSettings.getLanguage())) {
            account.setLanguage(accountSettings.getLanguage());
        }
        account.setPublicList(accountSettings.getPublicList());
        account.setNotifications(accountSettings.getNewsletter());
        accountService.update(account);
        accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/delete/{userID}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAccount(HttpServletRequest requ, HttpServletResponse resp, @PathVariable(value = "userID") String id) {
        boolean logout = false;
        Account account;
        try {
            account = accountService.findById(id);
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return ResponseEntity.notFound().build();
        }
        String message;
        if (!account.equals(Utils.getCurrentAccount())) {
            if (!account.getType().equals(AccountType.KID)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            //if not deleting his account
            Optional<Family> optionalFamily = familyService.getFamily(account);
            if (!optionalFamily.isPresent() || !optionalFamily.get().getAdmins().contains(Utils.getCurrentAccount())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            message = msgSrv.getMessage("user.family.delete.kid.success");
        } else {
            //deleting his own account
            logout = true;
            message = msgSrv.getMessage("user.delete.success");
        }
        eventService.deleteUserEvents(account);
        giftService.deleteClaims(account);
        giftService.deleteUserGifts(account);
        accountService.removeAllEvents(account);
        if (logout) {
            logoutHandler.logout(requ, resp, SecurityContextHolder.getContext().getAuthentication());
        }
        accountService.delete(account);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "admins", method = RequestMethod.GET)
    public ResponseEntity admins() {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(accountService.findAdmins());
    }

    private Account getAccountByUsernameOrId(@RequestParam(required = false) String identification) {
        Account account = null;
        Optional<Account> optionalAccount = accountService.findByUsername(identification);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
        } else {
            try {
                account = accountService.findById(identification);
            } catch (AccountNotFoundException e1) {
                LOG.error("Unable to find Account with id  or username. Identification used : {}", identification);
            }
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
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity shareGiftList(@RequestBody String emails) {
        if (!Utils.getCurrentAccount().getPublicList()) {
            return new ResultData.ResultBuilder().badReqest().error().build();
        }
        List<Mail> mailList = new ArrayList<>();
        List<String> emailLists = Arrays.stream(emails.split(";")).filter(Utils::validateEmail).collect(Collectors.toList());
        for (String email : emailLists) {
            Mail mail = new Mail();
            Optional<Account> opotionalAccount = accountService.findByEmail(email);
            mail.setMailTo(email);
            if (opotionalAccount.isPresent()) {
                Account byEmail = opotionalAccount.get();
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
        HashMap<String, String> model = new HashMap<>();
        model.put("emails", StringUtils.join(emailLists, ", "));
        return ResponseEntity.ok(model);
//        String message = msgSrv.getMessage("gift.share.success", new Object[]{StringUtils.join(emailLists, ", ")}, "", Utils.getCurrentLocale());
//        return new ResultData.ResultBuilder().ok().message(message).build();
    }

    //TODO delete afterwards
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/scheduler", method = RequestMethod.POST)
    public ResponseEntity sendScheduler() {
        try {
            mailService.sendEvents();
        } catch (MessagingException e) {
            LOG.error("Error while sending emailLists {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(e.getMessage()).build();
        }
        String message = msgSrv.getMessage("gift.share.success", null, "", Utils.getCurrentLocale());
        return new ResultData.ResultBuilder().ok().message(message).build();
    }
}
