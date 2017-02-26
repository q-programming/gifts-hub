package com.qprogramming.gifts.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remote on 26.02.2017.
 */
@Controller
public class MainController {

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
        return "login";
    }

    @RequestMapping("/list")
    public String list(Model model) {
        List<String> test = new ArrayList<>();
        test.add("one");
        test.add("two");
        model.addAttribute("myList", test);
        model.addAttribute("testmessage", "This is test message");
        return "list/list";
    }

}
