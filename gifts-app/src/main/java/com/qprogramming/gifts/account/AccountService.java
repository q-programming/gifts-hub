package com.qprogramming.gifts.account;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.account.authority.Authority;
import com.qprogramming.gifts.account.authority.AuthorityService;
import com.qprogramming.gifts.account.authority.Role;
import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.GiftService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

import static com.qprogramming.gifts.support.Utils.ACCOUNT_COMPARATOR;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountService implements UserDetailsService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());


    private AccountRepository _accountRepository;
    private AccountPasswordEncoder _accountPasswordEncoder;
    private AvatarRepository _avatarRepository;
    private FamilyService _familyService;
    private PropertyService _propertyService;
    private AccountEventRepository _accountEventRepository;
    private GiftService giftService;
    private AuthorityService _authorityService;


    @Autowired
    public AccountService(AccountRepository accountRepository, AccountPasswordEncoder accountPasswordEncoder, AvatarRepository avatarRepository, FamilyService familyService, PropertyService propertyService, AccountEventRepository accountEventRepository, GiftService giftService, AuthorityService authorityService) {
        this._accountRepository = accountRepository;
        this._accountPasswordEncoder = accountPasswordEncoder;
        this._avatarRepository = avatarRepository;
        this._familyService = familyService;
        this._propertyService = propertyService;
        this._accountEventRepository = accountEventRepository;
        this.giftService = giftService;
        this._authorityService = authorityService;
    }

    @PostConstruct
    protected void initialize() {
//        createLocalAccount(new Account("user", "demo", "ROLE_USER"));
//        createLocalAccount(new Account("admin", "admin", "ROLE_ADMIN"));
    }

    @Transactional
    public Account createLocalAccount(Account account) {
        account.setId(generateID());
        account.setPassword(_accountPasswordEncoder.encode(account.getPassword()));
        account.setUuid(Generators.timeBasedGenerator().generate().toString());
        account.setType(AccountType.LOCAL);
        return createAcount(account);
    }

    public Account createAcount(Account account) {
        List<Authority> auths = new ArrayList<>();
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

    public Account addAsAdministrator(Account account) {
        account.addAuthority(_authorityService.findByRole(Role.ROLE_ADMIN));
        return _accountRepository.save(account);
    }

    public Account removeAdministrator(Account account) {
        List<Authority> auths = new ArrayList<>();
        Authority role = _authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        account.setAuthorities(auths);
        return _accountRepository.save(account);
    }


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
        return optionalAccount.get();
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
     * @throws IOException
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
    public Account update(Account account) {
        return _accountRepository.save(account);
    }

    public Optional<Account> findByUsername(String username) {
        return _accountRepository.findOneByUsername(username);
    }

    public Optional<Account> findByEmail(String email) {
        return _accountRepository.findOneByEmail(email);
    }

    public List<Account> findAll() {
        return sortedAccounts(_accountRepository.findAll());
    }

    public List<Account> findAllWithNotifications() {
        return _accountRepository.findByNotificationsIsTrueAndEmailNotNullAndTypeIsNot(AccountType.KID);
    }


    /**
     * Search for all users within application.
     * If user has family , user from that family will be shown firsts. Other users are sorted per Surname/Name
     *
     * @param account account for which potential family will be used to sort
     * @return list of all sorted users
     */
    public Set<Account> findAllSortByFamily(Account account) {
        Family family = _familyService.getFamily(account);
        List<Account> list = findAll();
        Set<Account> result = new LinkedHashSet<>();
        if (family != null) {
            //Add all from user's family first
            result.addAll(list.stream().filter(family.getMembers()::contains).sorted(ACCOUNT_COMPARATOR).collect(Collectors.toList()));
            list.removeAll(result);
        }
        result.addAll(list);
        return result;
    }

    /**
     * Returns list of sorted accounts by name,surname,username
     *
     * @param list list of accounts to be sorted
     * @return List with accounts sorted by name,surname,username
     */
    public List<Account> sortedAccounts(List<Account> list) {
        return list.stream().sorted(ACCOUNT_COMPARATOR).collect(Collectors.toList());
    }

    /**
     * Return all accounts without family
     *
     * @return
     */
    public List<Account> findWithoutFamily() {
        List<Account> all = findAll();
        List<Account> accountsWithFamily = _familyService.findAll().stream().map(Family::getMembers).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        all.removeAll(accountsWithFamily);
        return all;
    }

    public List<Account> findByIds(List<String> members) {
        return _accountRepository.findByIdIn(members);
    }

    public void delete(Account account) {
        Family family = _familyService.getFamily(account);
        if (family != null) {
            _familyService.removeFromFamily(account, family);
        }
        Avatar avatar = _avatarRepository.findOneById(account.getId());
        if (avatar != null) {
            _avatarRepository.delete(avatar);
        }
        _accountRepository.delete(account);
    }

    public List<Account> findAdmins() {
        Authority adminRole = _authorityService.findByRole(Role.ROLE_ADMIN);
        return sortedAccounts(_accountRepository.findByAuthorities(adminRole));

    }

    public AccountEvent findEvent(String token) {
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
    public List<Account> findUsers() {
        return sortedAccounts(_accountRepository.findByTypeNot(AccountType.KID));

    }
}
