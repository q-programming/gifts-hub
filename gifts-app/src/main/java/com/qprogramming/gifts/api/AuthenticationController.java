package com.qprogramming.gifts.api;

import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.login.token.TokenService;
import com.qprogramming.gifts.login.token.UserTokenState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

    private TokenService _tokenService;

    private AuthenticationManager _authenticationManager;

    private AccountService _accountService;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    public AuthenticationController(TokenService tokenService, AuthenticationManager authenticationManager, AccountService accountService) {
        this._tokenService = tokenService;
        this._authenticationManager = authenticationManager;
        _accountService = accountService;
    }

//    @RequestMapping(value = "/auth", method = RequestMethod.POST)
//    public ResponseEntity createAuthenticationToken(
//            @RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletResponse response) throws AuthenticationException, IOException {
//        final Authentication authentication = _authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        authenticationRequest.getUsername(),
//                        authenticationRequest.getPassword()
//                )
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        Account account = (Account) authentication.getPrincipal();
//        _tokenService.createTokenCookies(response, account);
//        return ResponseEntity.ok().build();
//    }

//    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
//    @Transactional
//    public ResponseEntity register(
//            @RequestBody RegisterForm form) throws AuthenticationException {
//        Optional<Account> byEmail = _accountService.findByEmail(form.getEmail());
//        if (byEmail.isPresent()) {
//            return ResponseEntity.status(CONFLICT).body("email");
//        }
//        if (!form.getPassword().equals(form.getConfirmPassword())) {
//            return ResponseEntity.status(CONFLICT).body("passwords");
//        }
//        if (form.getPassword().length() < 8) {
//            return ResponseEntity.status(CONFLICT).body("weak");
//        }
//        try {
//            Account account = form.createAccount();
//            _accountService.createLocalAccount(account);
//            _accountService.sendConfirmEmail(account);
//        } catch (MessagingException e) {
//            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("mailing");
//        }
//        return ResponseEntity.ok().build();
//    }

//    @RequestMapping(value = "/auth/confirm", method = RequestMethod.GET)
//    public void confirmAccount(@RequestParam("token") String token, HttpServletRequest request, HttpServletResponse response) {
//        String uri = Utils.getFullPathFromRequest(request);
//        try {
//            try {
//                Account account = _accountService.findByUuid(token);
//                UUID uuid = UUID.fromString(account.getUuid());
//                DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
//                DateTime expireDate = date.plusHours(24);
//                if (date.isAfter(expireDate)) {
//                    _accountService.sendConfirmEmail(account);
//                    response.sendRedirect(uri + "/#/error?type=expired");
//                }
//                _accountService.confirm(account);
//                response.sendRedirect(uri + "/#/success?type=confirmed");
//            } catch (AccountNotFoundException e) {
//                response.sendRedirect(uri + "/#/error?type=account");
//            } catch (MessagingException e) {
//                LOG.error("Error while trying to send email{}", e);
//            }
//        } catch (IOException e) {
//            LOG.error("Error while trying to redirect {}", e);
//        }
//    }


    /**
     * Refreshes token (if it can be refreshed ) passed in request and returns back the token
     *
     * @param request  Request
     * @param response Response
     * @return refreshed token
     */
    @RequestMapping(value = "/api/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {

        String authToken = _tokenService.getToken(request);
        if (authToken != null && _tokenService.canTokenBeRefreshed(authToken)) {
            // TODO check user password last update
            String refreshedToken = _tokenService.refreshToken(authToken);
            _tokenService.refreshCookie(refreshedToken, response);
            UserTokenState userTokenState = new UserTokenState(refreshedToken, EXPIRES_IN);
            return ResponseEntity.ok(userTokenState);
        } else {
            UserTokenState userTokenState = new UserTokenState();
            return ResponseEntity.accepted().body(userTokenState);
        }
    }
}
