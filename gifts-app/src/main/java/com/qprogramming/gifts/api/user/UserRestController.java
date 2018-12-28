package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.RegisterForm;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.account.group.GroupForm;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.account.group.KidForm;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.exceptions.GroupNotAdminException;
import com.qprogramming.gifts.exceptions.GroupNotFoundException;
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

import static com.qprogramming.gifts.exceptions.AccountNotFoundException.ACCOUNT_WITH_ID_WAS_NOT_FOUND;
import static com.qprogramming.gifts.exceptions.GroupNotFoundException.GROUP_NOT_FOUND;

@RestController
@RequestMapping("/api/account")
public class UserRestController {

    public static final String PASSWORD_REGEXP = "^^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,}$";
    public static final String USERNAME_REGEXP = "^[a-zA-Z0-9_]+$";
    public static final String NEWSLETTER = "notifications";
    public static final String PUBLIC_LIST = "publicList";
    public static final String LANGUAGE = "language";
    private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    private AccountService _accountService;
    private MessagesService _msgSrv;
    private GroupService _groupService;
    private GiftService _giftService;
    private MailService _mailService;
    private AppEventService _eventService;
    private LogoutHandler _logoutHandler;

    @Autowired
    public UserRestController(AccountService accountService, MessagesService msgSrv, GroupService groupService, GiftService giftService, MailService mailService, AppEventService eventService, LogoutHandler logoutHandler) {
        this._accountService = accountService;
        this._msgSrv = msgSrv;
        this._groupService = groupService;
        this._giftService = giftService;
        this._mailService = mailService;
        this._eventService = eventService;
        this._logoutHandler = logoutHandler;
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
        if (_accountService.findByEmail(userform.getEmail()).isPresent()) {
            String message = _msgSrv.getMessage("user.register.email.exists") + " " + _msgSrv.getMessage("user.register.alreadyexists");
            return new ResponseEntity<>("email", HttpStatus.CONFLICT);
        }
        Pattern pattern = Pattern.compile(USERNAME_REGEXP);
        Matcher matcher = pattern.matcher(userform.getUsername());
        if (!matcher.matches()) {
            return new ResponseEntity<>("bad_username", HttpStatus.CONFLICT);
        }

        if (_accountService.findByUsername(userform.getUsername()).isPresent()) {
            return new ResponseEntity<>("username", HttpStatus.CONFLICT);
        }
        if (!userform.getPassword().equals(userform.getConfirmpassword())) {
            return new ResponseEntity<>("passwords", HttpStatus.CONFLICT);
        }
        if (userform.getPassword().length() < 8) {
            return new ResponseEntity<>("weak", HttpStatus.CONFLICT);
        }
        Account newAccount = userform.createAccount();
        newAccount = _accountService.createLocalAccount(newAccount);
//        _accountService.createAvatar(newAccount);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    @RequestMapping("/{username}/avatar")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userAvatar(@PathVariable(value = "username") String username) {
        Optional<Account> optionalAccount = _accountService.findByUsername(username);
        if (!optionalAccount.isPresent()) {
            LOG.error("Unable to find account with username {}", username);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(_accountService.getAccountAvatar(optionalAccount.get()));
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
        _accountService.updateAvatar(account, data);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Returns user list, if passed param users is true, will return only full accounts ( no kids)
     *
     * @param users if true, only users will be retured ( no KID accounts ) and gifts count's won't be added to response
     * @return list of application accounts
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<?> userList(@RequestParam(required = false) boolean users) {
        Set<Account> list = new HashSet<>();
        if (users) {
            list = new LinkedHashSet<>(_accountService.findUsers());
        } else {
            list = new LinkedHashSet<>(_accountService.findAll());
            addGiftCounts(list);
        }
        return ResponseEntity.ok(list);
    }

    @Transactional
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userSearchList(@RequestParam(required = false) String username, @RequestParam(required = false) boolean gifts) {
        try {
            Account account;
            if (StringUtils.isNotBlank(username)) {
                Optional<Account> optionalAccount = _accountService.findByUsername(username);
                account = optionalAccount.orElseThrow(AccountNotFoundException::new);
            } else {
                account = _accountService.getCurrentAccount();
            }
            Set<Account> list = _accountService.findAllFromGroups(account);
            if (gifts) {
                addGiftCounts(list);
            }
            return ResponseEntity.ok(list);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @RequestMapping("/groups")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<?> groupList() {
        try {
            Map<Account, Integer> counts = new HashMap<>();
            Account currentAccount = _accountService.getCurrentAccount();
            Set<Group> groups = currentAccount.getGroups();
            groups.forEach(group -> group.getMembers().forEach(account -> {
                Integer count = counts.computeIfAbsent(account, this::getGiftCount);
                account.setGiftsCount(count);
            }));
            return ResponseEntity.ok(groups);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Integer getGiftCount(Account account) {
        return _giftService.countAllByUser(account.getId());
    }

    private void addGiftCounts(Set<Account> list) {
        list.forEach(account -> account.setGiftsCount(_giftService.countAllByUser(account.getId())));
    }

    private void markAdmins(Group group) {
        group.getMembers().forEach(account -> {
            if (group.getAdmins().contains(account)) {
                account.setGroupAdmin(true);
            }
        });
    }

    /**
     * Returns currently logged in user group ( or null if not found )
     *
     * @return {@link Group}
     */
    //TODO remove ?
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getUserFamily(@RequestParam(required = false) String username) {
//        if (StringUtils.isNotBlank(username)) {
//            Optional<Account> account = _accountService.findByUsername(username);
//            if (!account.isPresent()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(_groupService.getGroup(account.get()).get());
//        }
//        return ResponseEntity.ok(_groupService.getGroup(Utils.getCurrentAccount()));
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns currently logged in user group ( or null if not found )
     *
     * @return {@link Group}
     */
    //TODO remove ?
    @RequestMapping(value = "/family/isAdmin", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> isUserGroupAdmin(@RequestParam String username) {
        if (StringUtils.isNotBlank(username)) {
            Optional<Account> account = _accountService.findByUsername(username);
            if (!account.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(_accountService.isAccountGroupAdmin(account.get()));
        }
        return ResponseEntity.notFound().build();
    }


    @Transactional
    @RequestMapping(value = "/group-create", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createGroup(@RequestBody GroupForm form) {
        try {
            Account currentAccount = _accountService.getCurrentAccount();
            if (StringUtils.isBlank(form.getName())) {
                form.setName(currentAccount.getSurname());
            }
            Group group = _groupService.createGroup(form.getName());
            group.addMember(currentAccount);
            group = _groupService.addAccountToGroupAdmins(currentAccount, group);
            Set<Account> members = _accountService.findByEmailsOrUsernames(form.getMembers());
            try {
                sendInvites(members, group, AccountEventType.GROUP_MEMEBER);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            HashMap<String, String> model = new HashMap<>();
            if (members.size() > 0) {
                model.put("result", "invites");
            } else {
                model.put("result", "ok");
            }
            return ResponseEntity.ok(model);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/group-update", method = RequestMethod.PUT)
    public ResponseEntity<?> updateGroup(@RequestBody GroupForm form) {
        try {
            Group group = _groupService.getGroupAsGroupAdmin(form.getId());
            //set members
            Set<Account> formMembers = _accountService.findByEmailsOrUsernames(form.getMembers());
            Set<Account> membersToInvite = formMembers.stream().filter(account -> !group.getMembers().contains(account)).collect(Collectors.toSet());
            formMembers.removeAll(membersToInvite);
            group.getMembers().retainAll(formMembers);
            //set admins
            Set<Account> formAdmins = _accountService.findByEmailsOrUsernames(form.getAdmins());
            Set<Account> adminsToInvite = formAdmins.stream().filter(account -> !group.getAdmins().contains(account)).collect(Collectors.toSet());
            formAdmins.removeAll(adminsToInvite);
            group.getAdmins().retainAll(formAdmins);
            //send invites, remove all double members if are in admin list
            try {
                membersToInvite.removeAll(adminsToInvite);
                sendInvites(membersToInvite, group, AccountEventType.GROUP_MEMEBER);
                sendInvites(adminsToInvite, group, AccountEventType.GROUP_ADMIN);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            if (StringUtils.isBlank(form.getName())) {
                form.setName(Utils.getCurrentAccount().getSurname());
            }
            group.setName(form.getName());
            HashMap<String, String> model = new HashMap<>();
            if (membersToInvite.size() > 0 || adminsToInvite.size() > 0) {
                model.put("result", "invites");
            } else {
                model.put("result", "ok");
            }
            return ResponseEntity.ok(model);
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (GroupNotAdminException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/group-leave", method = RequestMethod.PUT)
    public ResponseEntity<?> leaveGroup(@RequestBody GroupForm form) {
        try {
            Account currentAccount = _accountService.getCurrentAccount();
            Optional<Group> optionalGroup = _groupService.getGroupById(form.getId());
            if (!optionalGroup.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Group group = _groupService.removeFromGroup(currentAccount, optionalGroup.get());
            return ResponseEntity.ok(group);

        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/confirm")
    public ResponseEntity confirmOperation(@RequestBody String token) {
        UUID uuid = UUID.fromString(token);
        Optional<AccountEvent> eventOptional = _accountService.findEvent(token);
        if (!eventOptional.isPresent()) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        DateTime expireDate = date.plusDays(7);
        if (new DateTime().isAfter(expireDate)) {
            _accountService.removeEvent(event);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
        }
        if (!event.getType().equals(AccountEventType.ACCOUNT_CONFIRM) && !event.getAccount().equals(Utils.getCurrentAccount())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        switch (event.getType()) {
            case GROUP_MEMEBER:
                return handleBecomeGroupMember(event);
            case GROUP_ADMIN:
                return handleBecomeGroupAdmin(event);
            case ACCOUNT_CONFIRM:
                break;
        }
        return new ResultData.ResultBuilder().badReqest().build();
    }

    //TODO Remove
//    private ResponseEntity handleConfirmAllowFamily(AccountEvent event) {
//        try {
//            Group userGroup = _groupService.getGroupAsGroupAdmin();
//            if (event.getGroup() == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            }
//            Group targetGroup = event.getGroup();
//            targetGroup.getAllowedFamilies().add(userGroup.getId());
//            userGroup.getAllowedFamilies().add(targetGroup.getId());
//            _accountService.addAllowedToGroup(userGroup, targetGroup.getMembers());
//            _accountService.addAllowedToGroup(targetGroup, userGroup.getMembers());
//            _groupService.update(userGroup);
//            _groupService.update(targetGroup);
//            return ResponseEntity.ok().build();
//        } catch (GroupNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotAdminException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
//        }
//    }

    private ResponseEntity handleBecomeGroupAdmin(AccountEvent event) {
        try {
            Group group = _groupService.getGroupFromEvent(event);
            if (!group.getMembers().contains(Utils.getCurrentAccount())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            HashMap<String, String> model = new HashMap<>();
            _groupService.addAccountToGroupAdmins(Utils.getCurrentAccount(), group);
            _accountService.eventConfirmed(event);
            model.put("result", "family_admin");
            return ResponseEntity.ok(model);
        } catch (GroupNotFoundException e) {
            LOG.warn(GROUP_NOT_FOUND, event.getClass(), event.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private ResponseEntity handleBecomeGroupMember(AccountEvent event) {
        try {
            Group group = _groupService.getGroupFromEvent(event);
            Account currentAccount = _accountService.getCurrentAccount();
            group.addMember(currentAccount);
            HashMap<String, String> model = new HashMap<>();
            _accountService.eventConfirmed(event);
            model.put("result", "family_member");
            return ResponseEntity.ok(model);
        } catch (GroupNotFoundException e) {
            LOG.warn(GROUP_NOT_FOUND, event.getClass(), event.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccountNotFoundException e) {
            LOG.warn(ACCOUNT_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private void sendInvites(Set<Account> members, Group group, AccountEventType type) throws MessagingException {
        List<Account> list = new ArrayList<>(members);
        sendInvites(list, group, type);
    }

    private void sendInvites(List<Account> members, Group group, AccountEventType type) throws
            MessagingException {
        for (Account account : members) {
            if (AccountType.KID.equals(account.getType())) {
                group.addMember(account);
            } else if (AccountType.TEMP.equals(account.getType())) {
                Mail mail = Utils.createMail(account, Utils.getCurrentAccount());
                _mailService.sendInvite(mail, group.getName());
            } else {
                AccountEvent event = _groupService.inviteAccount(account, group, type);
                Mail mail = Utils.createMail(account, Utils.getCurrentAccount());
                _mailService.sendConfirmMail(mail, event);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/kid-add")
    public ResponseEntity<?> addKid(@RequestBody @Valid KidForm form) {
        Optional<Account> optionalAccount = _accountService.findByUsername(form.getUsername());
        if (optionalAccount.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username");
        }
        try {
            Group group = _groupService.getGroupAsGroupAdmin(form.getGroupId());
            Account kidAccount = form.createAccount();
            kidAccount = _accountService.createKidAccount(kidAccount);
            group.addMember(kidAccount);
            _groupService.update(group);
            if (StringUtils.isNotBlank(form.getAvatar())) {
                byte[] data = Base64.decodeBase64(form.getAvatar());
                _accountService.updateAvatar(kidAccount, data);
            }
            return ResponseEntity.ok(kidAccount);
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (GroupNotAdminException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
        }

    }

    //    @JsonView(MappingConfiguration.Members.class)
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/kid-update")
    public ResponseEntity<?> updateKid(@RequestBody @Valid KidForm form) {
        try {
            Group group = _groupService.getGroupAsGroupAdmin(form.getGroupId());
            Account kidAccount;
            kidAccount = _accountService.findById(form.getId());
            if (!group.getMembers().contains(kidAccount)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
            }
            if (StringUtils.isNotBlank(form.getName())) {
                kidAccount.setName(form.getName());
            }
            if (StringUtils.isNotBlank(form.getSurname())) {
                kidAccount.setSurname(form.getSurname());
            }
            kidAccount.setPublicList(form.getPublicList());
            _accountService.update(kidAccount);
            if (StringUtils.isNotBlank(form.getAvatar())) {
                byte[] data = Base64.decodeBase64(form.getAvatar());
                _accountService.updateAvatar(kidAccount, data);
            }
            return ResponseEntity.ok(kidAccount);
        } catch (AccountNotFoundException e) {
            LOG.error("Unable to find KID account with id {}", form.getId());
            return ResponseEntity.notFound().build();
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (GroupNotAdminException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
        }

    }


    @RequestMapping(value = "/validate-email", method = RequestMethod.POST)
    public ResponseEntity validateEmail(@RequestBody String email) {
        Optional<Account> acc = _accountService.findByEmail(email);
        if (!acc.isPresent()) {
            return ResponseEntity.ok(new ResultData.ResultBuilder().ok().build());
        }
        return ResponseEntity.ok(new ResultData.ResultBuilder().error().message("User exists").build());
    }

    @RequestMapping(value = "/validate-username", method = RequestMethod.POST)
    public ResponseEntity validateUsername(@RequestBody String username) {
        Optional<Account> optionalAccount = _accountService.findByUsername(username);
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
            account = _accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (account == null) {
        }
        Utils.getCurrentAccount().setTourComplete(true);
        account.setTourComplete(true);
        _accountService.update(account);
        _accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/tour-reset", method = RequestMethod.POST)
    public ResponseEntity resetTour() {
        Account account = null;
        try {
            account = _accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setTourComplete(false);
        account.setTourComplete(false);
        account = _accountService.update(account);
        _accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/changelog", method = RequestMethod.POST)
    public ResponseEntity changelogRead() {
        Account account;
        try {
            account = _accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        Utils.getCurrentAccount().setSeenChangelog(true);
        account.setSeenChangelog(true);
        account = _accountService.update(account);
        _accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity changeSettings(@RequestBody AccountSettings accountSettings) {
        //TODO rewrite ?
        Account account;
        try {
            account = _accountService.findById(Utils.getCurrentAccountId());
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (StringUtils.isNotBlank(accountSettings.getLanguage())) {
            account.setLanguage(accountSettings.getLanguage());
        }
        account.setPublicList(accountSettings.getPublicList());
        account.setNotifications(accountSettings.getNewsletter());
        _accountService.update(account);
        _accountService.signin(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/delete/{userID}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAccount(HttpServletRequest requ, HttpServletResponse
            resp, @PathVariable(value = "userID") String id) {
        boolean logout = false;
        Account account;
        try {
            account = _accountService.findById(id);
        } catch (AccountNotFoundException e) {
            LOG.error("Account with id {} not found", Utils.getCurrentAccountId());
            return ResponseEntity.notFound().build();
        }
        if (!account.equals(Utils.getCurrentAccount())) {
            if (!account.getType().equals(AccountType.KID) || !_accountService.isAccountGroupAdmin(account)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            //deleting his own account
            logout = true;
        }
        _eventService.deleteUserEvents(account);
        _giftService.deleteClaims(account);
        _giftService.deleteUserGifts(account);
        _accountService.removeAllEvents(account);
        account.getGroups().forEach(group -> _groupService.removeFromGroup(account, group));
        if (logout) {
            _logoutHandler.logout(requ, resp, SecurityContextHolder.getContext().getAuthentication());
        }
        _accountService.delete(account);
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
        return ResponseEntity.ok(_accountService.findAdmins());
    }

    private Account getAccountByUsernameOrId(@RequestParam(required = false) String identification) {
        Account account = null;
        Optional<Account> optionalAccount = _accountService.findByUsername(identification);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
        } else {
            try {
                account = _accountService.findById(identification);
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
            Optional<Account> opotionalAccount = _accountService.findByEmail(email);
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
            _mailService.shareGiftList(mailList);
        } catch (MessagingException e) {
            LOG.error("Error while sending emailLists {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(e.getMessage()).build();
        }
        HashMap<String, String> model = new HashMap<>();
        model.put("emails", StringUtils.join(emailLists, ", "));
        return ResponseEntity.ok(model);
//        String message = _msgSrv.getMessage("gift.share.success", new Object[]{StringUtils.join(emailLists, ", ")}, "", Utils.getCurrentLocale());
//        return new ResultData.ResultBuilder().ok().message(message).build();
    }

    //TODO remove
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/account/add", method = RequestMethod.PUT)
//    public ResponseEntity addAccountToAllowed(@RequestBody String accountID) {
//        try {
//            Account account = _accountService.findById(accountID);
//            Account currentAccount = _accountService.getCurrentAccount();
//            currentAccount.getAllowed().add(account.getId());
//            _accountService.update(currentAccount);
//            //TODO notify via email
//            return ResponseEntity.ok().build();
//        } catch (AccountNotFoundException e) {
//            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/account/remove", method = RequestMethod.DELETE)
//    public ResponseEntity removeAccountToAllowed(@RequestBody String accountID) {
//        try {
//            Account account = _accountService.findById(accountID);
//            Account currentAccount = _accountService.getCurrentAccount();
//            currentAccount.getAllowed().remove(account.getId());
//            _accountService.update(currentAccount);
//            //TODO notify via email
//            return ResponseEntity.ok().build();
//        } catch (AccountNotFoundException e) {
//            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
//            return ResponseEntity.notFound().build();
//        }
//    }


//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/group/add-account", method = RequestMethod.PUT)
//    public ResponseEntity addAccountToFamilyAllowed(@RequestBody String accountID) {
//        try {
//            Group userGroup = _groupService.getGroupAsGroupAdmin();
//            Account targetAccount = _accountService.findById(accountID);
//            userGroup.getAllowedAccounts().add(accountID);
//            _accountService.addAllowedToGroup(userGroup, Collections.singleton(targetAccount));
//            _groupService.update(userGroup);
//            //TODO notify target account
//            return ResponseEntity.ok().build();
//        } catch (AccountNotFoundException e) {
//            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, accountID);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotAdminException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
//        }
//    }
//
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/group/remove-account", method = RequestMethod.DELETE)
//    public ResponseEntity removeAccountFromFamilyAllowed(@RequestBody String accountID) {
//        try {
//            Group userGroup = _groupService.getGroupAsGroupAdmin();
//            Account targetAccount = _accountService.findById(accountID);
//            userGroup.getAllowedAccounts().remove(accountID);
//            _accountService.removeAllowedFromGroup(userGroup, Collections.singleton(targetAccount));
//            _groupService.update(userGroup);
//            //TODO notify target account
//            return ResponseEntity.ok().build();
//        } catch (AccountNotFoundException e) {
//            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, accountID);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotAdminException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
//        }
//    }

//    @Transactional
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/group/add-group", method = RequestMethod.DELETE)
//    public ResponseEntity addFamilyToAllowed(@RequestBody Long targetFamilyID) {
//        try {
//            Group userGroup = _groupService.getGroupAsGroupAdmin();
//            Optional<Group> optionalTargetFamily = _groupService.getGroupById(targetFamilyID);
//            if (!optionalTargetFamily.isPresent()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            }
//            Group targetGroup = optionalTargetFamily.get();
//            Set<AccountEvent> events = targetGroup.getAdmins()
//                    .stream()
//                    .map(account -> _groupService.groupAllowFamilyEvent(account, userGroup))
//                    .collect(Collectors.toSet());
//            for (AccountEvent event : events) {
//                Mail mail = Utils.createMail(event.getAccount(), Utils.getCurrentAccount());
//                _mailService.sendConfirmMail(mail, event);
//            }
//            return ResponseEntity.ok().build();
//        } catch (GroupNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotAdminException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
//        } catch (MessagingException e) {
//            LOG.error("Error while trying to send emails. {}", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    private void sendConfirmation(Set<AccountEvent> events) {
//    }
//
//    @Transactional
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/allowed/group/remove-group", method = RequestMethod.DELETE)
//    public ResponseEntity removeFamilyFromAllowed(@RequestBody Long targetFamilyID) {
//        try {
//            Group userGroup = _groupService.getGroupAsGroupAdmin();
//            Optional<Group> optionalTargetFamily = _groupService.getGroupById(targetFamilyID);
//            if (!optionalTargetFamily.isPresent()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            }
//            Group targetGroup = optionalTargetFamily.get();
//            targetGroup.getAllowedFamilies().remove(userGroup.getId());
//            userGroup.getAllowedFamilies().remove(targetGroup.getId());
//            _accountService.removeAllowedFromGroup(userGroup, targetGroup.getMembers());
//            _accountService.removeAllowedFromGroup(targetGroup, userGroup.getMembers());
//            _groupService.update(userGroup);
//            _groupService.update(targetGroup);
//            return ResponseEntity.ok().build();
//        } catch (GroupNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (GroupNotAdminException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("family_admin");
//        }
//    }

    //TODO delete afterwards
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/scheduler", method = RequestMethod.POST)
    public ResponseEntity sendScheduler() {
        try {
            _mailService.sendEvents();
        } catch (MessagingException e) {
            LOG.error("Error while sending emailLists {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(e.getMessage()).build();
        }
        String message = _msgSrv.getMessage("gift.share.success", null, "", Utils.getCurrentLocale());
        return new ResultData.ResultBuilder().ok().message(message).build();
    }
}
