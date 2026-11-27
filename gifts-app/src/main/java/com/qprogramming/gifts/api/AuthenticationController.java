package com.qprogramming.gifts.api;

import com.qprogramming.gifts.login.token.UserTokenState;
import com.qprogramming.gifts.security.TokenService;
import com.qprogramming.gifts.security.oauth2.KidConvertGoogleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 * <p>
 * Based on
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final TokenService _tokenService;
    private final KidConvertGoogleService _kidConvertGoogleService;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    public AuthenticationController(TokenService tokenService, KidConvertGoogleService kidConvertGoogleService) {
        this._tokenService = tokenService;
        this._kidConvertGoogleService = kidConvertGoogleService;
    }

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
            UserTokenState userTokenState = new UserTokenState(refreshedToken, (long) EXPIRES_IN);
            return ResponseEntity.ok(userTokenState);
        } else {
            UserTokenState userTokenState = new UserTokenState();
            return ResponseEntity.accepted().body(userTokenState);
        }
    }

    @RequestMapping(value = "/oauth2/kid-convert/google", method = RequestMethod.GET)
    public void startKidConvertWithGoogle(@RequestParam String token, @RequestParam("redirect_uri") String redirectUri,
                                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        _kidConvertGoogleService.start(token, redirectUri, request, response);
    }

    @RequestMapping(value = "/oauth2/kid-convert/callback", method = RequestMethod.GET)
    public void completeKidConvertWithGoogle(@RequestParam(required = false) String code, @RequestParam(required = false) String state,
                                             HttpServletRequest request, HttpServletResponse response) throws IOException {
        _kidConvertGoogleService.complete(code, state, request, response);
    }
}
