package com.qprogramming.gifts.account;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.authority.AuthorityService;
import com.qprogramming.gifts.account.authority.Role;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.support.Utils.ACCOUNT_COMPARATOR;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountService implements UserDetailsService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    private AccountRepository _accountRepository;
    private AccountPasswordEncoder _accountPasswordEncoder;
    private AvatarRepository _avatarRepository;
    private GroupService _groupService;
    private PropertyService _propertyService;
    private AccountEventRepository _accountEventRepository;
    private GiftService _giftService;
    private AuthorityService _authorityService;


    @Autowired
    public AccountService(AccountRepository accountRepository, AccountPasswordEncoder accountPasswordEncoder, AvatarRepository avatarRepository, GroupService groupService, PropertyService propertyService, AccountEventRepository accountEventRepository, GiftService giftService, AuthorityService authorityService) {
        this._accountRepository = accountRepository;
        this._accountPasswordEncoder = accountPasswordEncoder;
        this._avatarRepository = avatarRepository;
        this._groupService = groupService;
        this._propertyService = propertyService;
        this._accountEventRepository = accountEventRepository;
        this._giftService = giftService;
        this._authorityService = authorityService;
    }

    @PostConstruct
    protected void initialize() {
//        createLocalAccount(new Account("user", "demo", "ROLE_USER"));
//        createLocalAccount(new Account("admin", "admin", "ROLE_ADMIN"));
    }

    @Transactional
    @CacheEvict(value = {"accounts", "groups"}, allEntries = true)
    public Account createLocalAccount(Account account) {
        account.setId(generateID());
        encodePassword(account);
        account.setUuid(Generators.timeBasedGenerator().generate().toString());
        account.setType(AccountType.LOCAL);
        setLocale(account);
        return createAcount(account);
    }

    public Account createAcount(Account account) {
        Set<Authority> auths = new HashSet<>();
        Authority role = _authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        if (_accountRepository.findAll().size() == 0) {
            Authority admin = _authorityService.findByRole(Role.ROLE_ADMIN);
            auths.add(admin);
        }
        account.setAuthorities(auths);
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        //generate password if needed
        return _accountRepository.save(account);
    }

    public void setLocale(Account account) {
        setLocale(account, null);
    }

    public void setLocale(Account account, String locale) {
        if (StringUtils.isBlank(locale) || _propertyService.getLanguages().keySet().contains(locale)) {
            account.setLanguage(locale);
        } else {
            locale = _propertyService.getDefaultLang();
            setLocale(account, locale);
        }
    }

    /**
     * Encodes password passed in plain text in Account
     *
     * @param account Account for which password will be encoded
     */
    public void encodePassword(Account account) {
        account.setPassword(_accountPasswordEncoder.encode(account.getPassword()));
    }


    public Account addAsAdministrator(Account account) {
        account.addAuthority(_authorityService.findByRole(Role.ROLE_ADMIN));
        return _accountRepository.save(account);
    }

    public Account removeAdministrator(Account account) {
        Set<Authority> auths = new HashSet<>();
        Authority role = _authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        account.setAuthorities(auths);
        return _accountRepository.save(account);
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    public Account createKidAccount(Account account) {
        account.setId(generateID());
        account.setType(AccountType.KID);
        return _accountRepository.save(account);
    }

    private void setDefaultLocale(Account account) {
        String defaultLanguage = _propertyService.getDefaultLang();
        account.setLanguage(defaultLanguage);
    }

    public String generateID() {
        String uuid = UUID.randomUUID().toString();
        while (_accountRepository.findOneById(uuid).isPresent()) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> optionalAccount = _accountRepository.findOneByEmail(username);
        if (!optionalAccount.isPresent()) {
            optionalAccount = _accountRepository.findOneByUsername(username);
            if (!optionalAccount.isPresent() || AccountType.KID.equals(optionalAccount.get().getType())) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        Account account = optionalAccount.get();
        //TODO Depreciated part fix. Due to changes in roles if none of authorities were found add them
        if (account.getAuthorities().isEmpty()) {
            Authority role = _authorityService.findByRole(Role.ROLE_USER);
            account.addAuthority(role);
            account = update(account);
        }
        return account;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    public Account findById(String id) throws AccountNotFoundException {
        Optional<Account> optionalAccount = _accountRepository.findOneById(id);
        if (!optionalAccount.isPresent()) {
            throw new AccountNotFoundException();
        }
        return optionalAccount.get();
    }

    //User avatar handling
    public Avatar getAccountAvatar(Account account) {
        return _avatarRepository.findOneById(account.getId());
    }

    /**
     * Update user avatar with passed bytes.
     * In case of avatar was not there, it will be created out of passed bytes
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account updated account
     * @param bytes   image bytes
     */
    public void updateAvatar(Account account, byte[] bytes) {
        Avatar avatar = _avatarRepository.findOneById(account.getId());
        if (avatar == null) {
            createAvatar(account, bytes);
        } else {
            setAvatarTypeAndBytes(bytes, avatar);
            _avatarRepository.save(avatar);
        }
    }

    /**
     * Creates new avatar from given URL
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account account for which avatar is created
     * @param url     url from which avatar image will be fetched
     * @return new {@link Avatar}
     * @throws MalformedURLException
     */
    public Avatar createAvatar(Account account, String url) throws MalformedURLException {
        byte[] bytes = downloadFromUrl(new URL(url));
        return createAvatar(account, bytes);
    }


    /**
     * Creates avatar from bytes
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account Account for which avatar is created
     * @param bytes   bytes containing avatar
     * @return new {@link Avatar}
     */
    public Avatar createAvatar(Account account, byte[] bytes) {
        Avatar avatar = new Avatar();
        avatar.setId(account.getId());
        setAvatarTypeAndBytes(bytes, avatar);
        return _avatarRepository.save(avatar);
    }

    private void setAvatarTypeAndBytes(byte[] bytes, Avatar avatar) {
        avatar.setImage(bytes);
        String type = "";
        try {
            type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOG.error("Failed to determine type from bytes, presuming jpg");
        }
        if (StringUtils.isEmpty(type)) {
            type = MediaType.IMAGE_JPEG_VALUE;
        }
        avatar.setType(type);
    }

    /**
     * !Visible for testing
     *
     * @param url - url from which bytes will be transfered
     * @return byte array of image
     */
    protected byte[] downloadFromUrl(URL url) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream stream = url.openStream()) {
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            LOG.error("Failed to download from URL ");
            return null;
        }
        return outputStream.toByteArray();
    }

    /**
     * Just save passed account
     *
     * @param account account to be saved
     * @return updated account
     */
    @CacheEvict(value = {"accounts", "groups"}, allEntries = true)
    public Account update(Account account) {
        return _accountRepository.save(account);
    }

    public Optional<Account> findByUsername(String username) {
        return _accountRepository.findOneByUsername(username);
    }

    public Optional<Account> findByEmail(String email) {
        return _accountRepository.findOneByEmail(email);
    }

    public Set<Account> findAll() {
        return sortedAccounts(_accountRepository.findAll());
    }

    public List<Account> findAllWithNotifications() {
        return _accountRepository.findByNotificationsIsTrueAndEmailNotNullAndTypeIsNot(AccountType.KID);
    }


    /**
     * Search for all users that are member of group that account belongs to
     *
     * @param account account for which group will be used to fetch all member
     * @return list of all sorted users
     */
    @Cacheable("accounts")
    public Set<Account> findAllFromGroups(Account account) {
        Set<Account> accounts = _accountRepository.findByGroupsIn(account.getGroups());
        TreeSet<Account> result = new TreeSet<>(ACCOUNT_COMPARATOR);
        result.addAll(accounts);
        result.add(account);
        return result;
    }

    /**
     * Returns list of sorted accounts by name,surname,username
     *
     * @param list list of accounts to be sorted
     * @return List with accounts sorted by name,surname,username
     */
    public Set<Account> sortedAccounts(List<Account> list) {
        return list.stream().collect(Collectors.toCollection(() -> new TreeSet<>(ACCOUNT_COMPARATOR)));
    }

    public Set<Account> findByIds(Set<String> members) {
        return _accountRepository.findByIdIn(members);
    }

    /**
     * Finds all accounts by emails. If account was not found in database , new fake temp account will be added to returned list
     * This is so that the temp accounts can recieve invitiation to application later on
     *
     * @param id list of emails or usernames
     * @return list of all accounts both DB presnet and temp ones
     */
    public Set<Account> findByEmailsOrUsernames(Set<String> id) {
        Set<Account> accounts = new HashSet<>();
        if (!id.isEmpty()) {
            accounts = _accountRepository.findByEmailIn(id);
            accounts.addAll(_accountRepository.findByUsernameIn(id));
            id.removeAll(accounts.stream().map(Account::getEmail).collect(Collectors.toSet()));
            id.removeAll(accounts.stream().map(Account::getUsername).collect(Collectors.toSet()));
            if (!id.isEmpty()) {
                accounts.addAll(id.stream().filter(s -> VALID_EMAIL_ADDRESS_REGEX.matcher(s).find()).map(Account::new).collect(Collectors.toSet()));
            }
        }
        return accounts;
    }

    public void delete(Account account) {
        Set<Group> allAccountGroups = _groupService.findAllAccountGroups(account);
        allAccountGroups.forEach(group -> {
            _groupService.removeFromGroup(account, group);
        });
        Avatar avatar = _avatarRepository.findOneById(account.getId());
        if (avatar != null) {
            _avatarRepository.delete(avatar);
        }
        account.setAuthorities(new HashSet<>());
        _accountRepository.delete(account);
    }

    public Set<Account> findAdmins() {
        Authority adminRole = _authorityService.findByRole(Role.ROLE_ADMIN);
        return sortedAccounts(_accountRepository.findByAuthorities(adminRole));

    }

    public Optional<AccountEvent> findEvent(String token) {
        return _accountEventRepository.findByToken(token);
    }


    public void eventConfirmed(AccountEvent event) {
        _accountEventRepository.delete(event);
    }

    /**
     * Find All Accounts that are not type KID
     *
     * @return sorted list of all accounts other than KID type
     */
    public Set<Account> findUsers() {
        return sortedAccounts(_accountRepository.findByTypeNot(AccountType.KID));

    }

    /**
     * Removes all accounts events for passed account
     * Used while deleting account
     *
     * @param account account for which events will be deleted
     */
    public void removeAllEvents(Account account) {
        List<AccountEvent> accountEvents = _accountEventRepository.findAllByAccount(account);
        _accountEventRepository.deleteAll(accountEvents);
    }

    /**
     * Remove AccountEvent ( for example after token was used)
     *
     * @param event AccountEvent to be removed
     */
    public void removeEvent(AccountEvent event) {
        _accountEventRepository.delete(event);
    }

    public Account getCurrentAccount() throws AccountNotFoundException {
        return findById(Utils.getCurrentAccountId());
    }

    public Set<Account> update(Set<Account> members) {
        return new HashSet<>(_accountRepository.saveAll(members));
    }

    public boolean isAccountGroupMember(Account account) {
        return account.getGroups().stream().anyMatch(group -> group.getMembers().contains(Utils.getCurrentAccount()));
    }

    public boolean isAccountGroupAdmin(Account account) {
        return account.getGroups().stream().anyMatch(group -> group.getAdmins().contains(Utils.getCurrentAccount()));
    }

    public boolean isKidAdmin(Account kid) {
        return kid.getType().equals(AccountType.KID) && isAccountGroupAdmin(kid);
    }


    public AccountEvent createGroupInviteEvent(Account account, Group group, AccountEventType type) {
        AccountEvent event = new AccountEvent();
        event.setAccount(account);
        event.setGroup(group);
        event.setType(type);
        event.setToken(generateToken());
        return _accountEventRepository.save(event);
    }

    public AccountEvent createConfirmEvent(Account newAccount) {
        AccountEvent event = new AccountEvent();
        event.setAccount(newAccount);
        event.setType(AccountEventType.ACCOUNT_CONFIRM);
        event.setToken(generateToken());
        return _accountEventRepository.save(event);
    }

    public AccountEvent createPasswordResetEvent(Account newAccount) {
        AccountEvent event = new AccountEvent();
        event.setAccount(newAccount);
        event.setType(AccountEventType.PASSWORD_RESET);
        event.setToken(generateToken());
        return _accountEventRepository.save(event);
    }


    public String generateToken() {
        String token = Generators.timeBasedGenerator().generate().toString();
        while (_accountEventRepository.findByToken(token).isPresent()) {
            token = Generators.timeBasedGenerator().generate().toString();
        }
        return token;
    }

    @Cacheable("groups")
    public Set<Group> getGroupsForAccount(Account account) {
        Map<Account, Integer> counts = new HashMap<>();
        Set<Group> groups = account.getGroups();
        groups.forEach(group -> {
            group.setMembers(new TreeSet<>(group.getMembers()));
            group.getMembers().forEach(acc -> {
                Integer count = counts.computeIfAbsent(acc, this::getGiftCount);
                acc.setGiftsCount(count);
            });
        });
        return groups;
    }

    private Integer getGiftCount(Account account) {
        return _giftService.countAllByAccountId(account.getId());
    }
}
