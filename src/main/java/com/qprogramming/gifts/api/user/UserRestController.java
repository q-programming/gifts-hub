package com.qprogramming.gifts.api.user;

import com.qprogramming.gifts.account.DisplayAccount;
import com.qprogramming.gifts.account.RegisterForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody RegisterForm userform) {
        System.out.println("name:" + userform.getName());
        System.out.println("surname:" + userform.getSurname());
        return "OK";
    }

    @RequestMapping(value = "/validate-email", method = RequestMethod.POST)
    public String validateEmail(@RequestBody String email) {
        return "OK";
    }

    @RequestMapping("/")
    public DisplayAccount user(Principal user) {
        DisplayAccount acc = new DisplayAccount();
        //TODO move filling of user to LoginSuccessHanler
        if (user != null && user instanceof OAuth2Authentication) {
            Map<String, String> details = (Map) ((OAuth2Authentication) user).getUserAuthentication().getDetails();
            acc.setName(details.get("name"));
            acc.setId(details.get("id"));
            if (StringUtils.isBlank(acc.getId())) {
                acc.setId(details.get("sub"));
            }
            acc.setEmail(details.get("email"));
            acc.setPhotoUrl(details.get("picture"));
//            if facebook data not recived
//            if (acc.getPhotoUrl() == null) {
//                FBGraph fbGraph = new FBGraph(((OAuth2AuthenticationDetails) ((OAuth2Authentication) user).getDetails()).getTokenValue());
//                String graph = fbGraph.getFBGraph();
//                Map<String, String> graphData = fbGraph.getGraphData(graph);
//                acc.setEmail(graphData.get("email"));
//            }
        } else if (user != null) {
            acc.setName(user.getName());
        }
        return acc;
    }

}
