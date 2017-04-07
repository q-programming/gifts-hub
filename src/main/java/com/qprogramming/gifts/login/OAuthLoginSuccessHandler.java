package com.qprogramming.gifts.login;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.Roles;
import com.qprogramming.gifts.login.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Created by Remote on 05.03.2017.
 */
@Service
public class OAuthLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {


    public static final String EMAIL = "email";
    public static final String LOCALE = "locale";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private AccountService accountService;
    private TokenService tokenService;

    @Autowired
    public OAuthLoginSuccessHandler(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        //check if signed in with google first (all details are there )
        Map<String, String> details = (Map) ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
        Account account;
        if (details.containsKey(G.SUB)) {//google+
            String userId = details.get(G.SUB);
            account = accountService.findById(userId);
            if (account == null) {//no profile yet, create
                account = createGoogleAccount(details, userId);
            }
        } else {//facebook , need to fetch data
            String userId = details.get(FB.ID);
            account = accountService.findById(userId);
            if (account == null) {
                account = createFacebookAccount(authentication, userId);
            }
        }
        accountService.signin(account);
        //token cookie creation
        tokenService.createTokenCookies(response, account);
        LOG.info("Login success for user: " + account.getId());
        super.onAuthenticationSuccess(request, response, authentication);
    }


    private Account createFacebookAccount(Authentication authentication, String userId) {
        Account account;
        String token = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        Facebook facebook = getFacebookTemplate(token);
        String[] fields = {FB.ID, EMAIL, FB.FIRST_NAME, FB.LAST_NAME, LOCALE};
        User facebookUser = facebook.fetchObject(FB.ME, User.class, fields);
        account = new Account();
        account.setType(AccountType.FACEBOOK);
        account.setId(userId);
        account.setName(facebookUser.getFirstName());
        account.setSurname(facebookUser.getLastName());
        account.setEmail(facebookUser.getEmail());
        setUsername(account);
        String locale = facebookUser.getLocale().getLanguage();
        setLocale(account, locale);
        byte[] userProfileImage = facebook.userOperations().getUserProfileImage();
        accountService.createAvatar(account, userProfileImage);
        account.setRole(Roles.ROLE_USER);
        account = accountService.update(account);
        LOG.debug("Facebook account has been created with id:{} and username{}", account.getId(), account.getUsername());
        return account;
    }


    Facebook getFacebookTemplate(String token) {
        return new FacebookTemplate(token);
    }

    private Account createGoogleAccount(Map<String, String> details, String userId) throws MalformedURLException {
        Account account;
        account = new Account();
        account.setId(userId);
        account.setType(AccountType.GOOGLE);
        account.setName(details.get(G.GIVEN_NAME));
        account.setSurname(details.get(G.FAMILY_NAME));
        account.setEmail(details.get(EMAIL));
        setUsername(account);
        String locale = details.get(LOCALE);
        setLocale(account, locale);
        accountService.createAvatar(account, details.get(G.PICTURE));
        account.setRole(Roles.ROLE_USER);
        account = accountService.update(account);
        LOG.debug("Google+ account has been created with id:{} and username{}", account.getId(), account.getUsername());
        return account;
    }

    private void setUsername(Account account) {
        String username = account.getEmail().split("@")[0];
        Account exists = accountService.findByUsername(username);
        if (exists != null) {
            username += "_" + account.getType().getCode();
        }
        account.setUsername(username);
    }

    private void setLocale(Account account, String locale) {
        if (locale.equals("en") || locale.equals("pl")) {
            account.setLanguage(locale);
        } else {
            account.setLanguage("en");
        }
    }

    class FB {
        public static final String ID = "id";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String ME = "me";

    }

    class G {
        public static final String SUB = "sub";
        public static final String GIVEN_NAME = "given_name";
        public static final String FAMILY_NAME = "family_name";
        public static final String PICTURE = "picture";
    }


}