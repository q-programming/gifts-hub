package com.qprogramming.gifts.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Remote on 26.02.2017.
 */
@Controller
public class TemplatesController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/home")
    public String home() {
        return "home";
    }

    @RequestMapping("/login")
    public String login() {
        return "user/login";
    }

    @RequestMapping("/register")
    public String register() {
        return "user/register";
    }

//    @RequestMapping("/list")
//    public String list(Model model) {
//        List<String> test = new ArrayList<>();
//        test.add("one");
//        test.add("two");
//        model.addAttribute("myList", test);
//        model.addAttribute("testmessage", "This is test message");
//        return "list/list";
//    }

    @RequestMapping("/list")
    public String list() {
        return "gifts/list";
    }


    @RequestMapping("/settings")
    public String settings(Model model) {
        //TODO add real languages map after localisation is done
        Map<String, String> languages = new HashMap<>();
        languages.put("pl", "Polski");
        languages.put("en", "English");
        model.addAttribute("languages", languages);
        return "user/settings";
    }

}
