package com.qprogramming.gifts.main;

import com.qprogramming.gifts.config.property.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.RolesAllowed;
import java.util.Map;

import static com.qprogramming.gifts.config.property.Property.APP_DEFAULT_LANG;

/**
 * Created by Remote on 26.02.2017.
 */
@Controller
public class TemplatesController {

    private PropertyService propertyService;
    private Environment env;

    @Autowired
    public TemplatesController(PropertyService propertyService, Environment env) {
        this.propertyService = propertyService;
        this.env = env;
    }


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

    @RequestMapping("/userlits")
    public String userList() {
        return "user/list";
    }

    @RequestMapping("/manage")
    @RolesAllowed("ROLE_ADMIN")
    public String manage(Model model) {
        Map<String, String> languages = propertyService.getLanguages();
        model.addAttribute("languages", languages);
        model.addAttribute("appLang", env.getProperty(APP_DEFAULT_LANG));
        return "app/manage";
    }

    @RequestMapping("/settings")
    public String settings(Model model) {

        //TODO add real languages map after localisation is done
        Map<String, String> languages = propertyService.getLanguages();
        model.addAttribute("languages", languages);
        return "user/settings";
    }

}
