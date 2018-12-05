package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.config.property.PropertyService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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


    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private AvatarRepository avatarRepository;
    private FamilyService familyService;
    private PropertyService propertyService;
    private AccountEventRepository accountEventRepository;
    private GiftService giftService;


    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, AvatarRepository avatarRepository, FamilyService familyService, PropertyService propertyService, AccountEventRepository accountEventRepository, GiftService giftService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.avatarRepository = avatarRepository;
        this.familyService = familyService;
        this.propertyService = propertyService;
        this.accountEventRepository = accountEventRepository;
        this.giftService = giftService;
    }

    @PostConstruct
    protected void initialize() {
//        createLocalAccount(new Account("user", "demo", "ROLE_USER"));
//        createLocalAccount(new Account("admin", "admin", "ROLE_ADMIN"));
    }

    @Transactional
    public Account createLocalAccount(Account account) {
        account.setId(generateID());
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        if (accountRepository.findAll().size() == 0) {
            account.setRole(Roles.ROLE_ADMIN);
        } else {
            account.setRole(Roles.ROLE_USER);
        }
        account.setType(AccountType.LOCAL);
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        return accountRepository.save(account);
    }

    public Account createOAuthAcount(Account account) {
        if (accountRepository.findAll().size() == 0) {
            account.setRole(Roles.ROLE_ADMIN);
        } else {
            account.setRole(Roles.ROLE_USER);
        }
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        return accountRepository.save(account);
    }

    public Account createKidAccount(Account account) {
        account.setId(generateID());
        account.setType(AccountType.KID);
        return accountRepository.save(account);
    }

    private void setDefaultLocale(Account account) {
        String defaultLanguage = propertyService.getDefaultLang();
        account.setLanguage(defaultLanguage);
    }

    public String generateID() {
        String uuid = UUID.randomUUID().toString();
        while (accountRepository.findOneById(uuid) != null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByEmail(username);
        if (account == null) {
            account = accountRepository.findOneByUsername(username);
            if (account == null || AccountType.KID.equals(account.getType())) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        account.setAuthority(account.getRole());
        return account;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, Collections.singleton(createAuthority(account)));
    }

//    private Account createUser(Account account) {
//        return new User(account.getEmail(), account.getPassword(), Collections.singleton(createAuthority(account)));
//    }

    private GrantedAuthority createAuthority(Account account) {
        return new SimpleGrantedAuthority(account.getRole().toString());
    }

    public Account findById(String id) {
        return accountRepository.findOneById(id);
    }

    //User avatar handling

    public Avatar getAccountAvatar(Account account) {
        return avatarRepository.findOneById(account.getId());
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
        Avatar avatar = avatarRepository.findOneById(account.getId());
        if (avatar == null) {
            createAvatar(account, bytes);
        } else {
            setAvatarTypeAndBytes(bytes, avatar);
            avatarRepository.save(avatar);
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
        return avatarRepository.save(avatar);
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
        return accountRepository.save(account);
    }

    public Account findByUsername(String username) {
        return accountRepository.findOneByUsername(username);
    }

    public Account findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    public List<Account> findAll() {
        return sortedAccounts(accountRepository.findAll());
    }

    public List<Account> findAllWithNewsletter() {
        return accountRepository.findByNewsletterIsTrueAndEmailNotNullAndTypeIsNot(AccountType.KID);
    }


    /**
     * Search for all users within application.
     * If user has family , user from that family will be shown firsts. Other users are sorted per Surname/Name
     *
     * @param account account for which potential family will be used to sort
     * @return list of all sorted users
     */
    public Set<Account> findAllSortByFamily(Account account) {
        Family family = familyService.getFamily(account);
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
        List<Account> accountsWithFamily = familyService.findAll().stream().map(Family::getMembers).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        all.removeAll(accountsWithFamily);
        return all;
    }

    public List<Account> findByIds(List<String> members) {
        return accountRepository.findByIdIn(members);
    }

    public void delete(Account account) {
        Family family = familyService.getFamily(account);
        if (family != null) {
            familyService.removeFromFamily(account, family);
        }
        Avatar avatar = avatarRepository.findOneById(account.getId());
        if (avatar != null) {
            avatarRepository.delete(avatar);
        }
        accountRepository.delete(account);
    }

    public List<Account> findAdmins() {
        return sortedAccounts(accountRepository.findByRole(Roles.ROLE_ADMIN));

    }

    public AccountEvent findEvent(String token) {
        return accountEventRepository.findByToken(token);
    }


    public void eventConfirmed(AccountEvent event) {
        accountEventRepository.delete(event);
    }

    /**
     * Find All Accounts that are not type KID
     *
     * @return sorted list of all accounts other than KID type
     */
    public List<Account> findUsers() {
        return sortedAccounts(accountRepository.findByTypeNot(AccountType.KID));

    }
}
