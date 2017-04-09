package com.qprogramming.gifts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class GiftsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(GiftsApplication.class, args);
    }

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(GiftsApplication.class);
//    }
}
