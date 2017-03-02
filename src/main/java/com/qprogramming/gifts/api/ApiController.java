package com.qprogramming.gifts.api;

import com.qprogramming.gifts.account.DisplayAccount;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiController {

    @RequestMapping("/resource")
    public Map<String, Object> home() {
        Map<String, Object> model = new HashMap<>();
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello World");
        return model;
    }

    @RequestMapping("/user")
    public DisplayAccount user(Principal user) {
        DisplayAccount acc = new DisplayAccount();
        if (user != null && user instanceof OAuth2Authentication) {
            Map<String, String> details = (Map) ((OAuth2Authentication) user).getUserAuthentication().getDetails();
            acc.setName(details.get("name"));
            acc.setId(details.get("id"));
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
