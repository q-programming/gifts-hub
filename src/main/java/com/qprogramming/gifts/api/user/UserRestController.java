package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.RegisterForm;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    private AccountService accountService;

    @Autowired
    public UserRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody RegisterForm userform) {
        //TODO validation
        Account formAccount = userform.createAccount();
        formAccount = accountService.create(formAccount);
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            InputStream avatarFile = loader.getResourceAsStream("static/images/logo-white.png");
            byte[] bytes = IOUtils.toByteArray(avatarFile);
            accountService.createAvatar(formAccount, bytes);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return ResponseEntity.badRequest().body("Failed to read default avatar file");
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping("/{id}/avatar")
    public ResponseEntity<?> user(@PathVariable(value = "id") String id) {
        Account account = accountService.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountService.getAccountAvatar(account));
    }


    @RequestMapping(value = "/validate-email", method = RequestMethod.POST)
    public ResponseEntity validateEmail(@RequestBody String email) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/")
    public Account user(Principal user) {
        //TODO move filling of user to LoginSuccessHandler and save to DB
        //TODO move to PrincipalExtractor
        if (user != null && user instanceof OAuth2Authentication) {
            Account account = new Account();
            Map<String, String> details = (Map) ((OAuth2Authentication) user).getUserAuthentication().getDetails();
            account.setName(details.get("name"));
            account.setId(details.get("id"));
            if (StringUtils.isBlank(account.getId())) {
                account.setId(details.get("sub"));
            }
            account.setEmail(details.get("email"));
            //TODO save picture to DB , refresh once a week
//            account.setAvatar(details.get("picture"));
//            if facebook data not recived
//            if (acc.getAvatar() == null) {
//                FBGraph fbGraph = new FBGraph(((OAuth2AuthenticationDetails) ((OAuth2Authentication) user).getDetails()).getTokenValue());
//                String graph = fbGraph.getFBGraph();
//                Map<String, String> graphData = fbGraph.getGraphData(graph);
//                acc.setEmail(graphData.get("email"));
//            }
            return account;
        } else if (user != null && user instanceof UsernamePasswordAuthenticationToken) {
            return (Account) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
        }
        return null;
    }

}
