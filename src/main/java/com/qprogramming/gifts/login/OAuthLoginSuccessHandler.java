package com.qprogramming.gifts.login;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.Roles;
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

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private AccountService accountService;

    @Autowired
    public OAuthLoginSuccessHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        //check if signed in with google first (all details are there )
        Map<String, String> details = (Map) ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
        Account account;
        if (details.containsKey("sub")) {//google+
            String userId = details.get("sub");
            account = accountService.findById(userId);
            if (account == null) {//no profile yet, create
                account = createGoogleAccount(details, userId);
            }
        } else {//facebook , need to fetch data
            String userId = details.get("id");
            account = accountService.findById(userId);
            if (account == null) {
                account = createFacebookAccount(authentication, userId);
            }
        }
        accountService.signin(account);
        LOG.info("Login success for user: " + account.getId());
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private Account createFacebookAccount(Authentication authentication, String userId) {
        Account account;
        String token = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        Facebook facebook = new FacebookTemplate(token);
        String[] fields = {"id", "email", "first_name", "last_name", "locale"};
        User facebookUser = facebook.fetchObject("me", User.class, fields);
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

    private Account createGoogleAccount(Map<String, String> details, String userId) throws MalformedURLException {
        Account account;
        account = new Account();
        account.setId(userId);
        account.setType(AccountType.GOOGLE);
        account.setName(details.get("given_name"));
        account.setSurname(details.get("family_name"));
        account.setEmail(details.get("email"));
        setUsername(account);
        String locale = details.get("locale");
        setLocale(account, locale);
        accountService.createAvatar(account, details.get("picture"));
        account.setRole(Roles.ROLE_USER);
        account = accountService.update(account);
        LOG.debug("Google+ account has been created with id:{} and username{}", account.getId(), account.getUsername());
        return account;
    }

    private void setUsername(Account account) {
        String username = account.getEmail().split("@")[0];
        Account exists = accountService.findByUsername(username);
        if (exists != null) {
            username += "." + account.getType().getCode();
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


}