package com.qprogramming.gifts.api.messages;

import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Properties;

@RestController
@RequestMapping("/api/messages")
public class MessagesRestController {
    private final MessagesService msgSrv;

    public MessagesRestController(MessagesService msgSrv) {
        this.msgSrv = msgSrv;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Properties list(@RequestParam(required = false) String lang) {
        if (StringUtils.isNotBlank(lang)) {
            return msgSrv.getAllProperties(new Locale(lang));
        }
        return msgSrv.getAllProperties(Utils.getCurrentLocale());
    }
}
