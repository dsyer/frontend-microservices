package com.example;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class ResourceApplication extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:templates/");
    }

    public static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);
    }
}

@Controller
@RequestMapping("/")
class HomeController {
    @GetMapping
    public String home() {
        return "index";
    }
}

@RestController
@RequestMapping("/resource")
class ResourceController {
    @GetMapping
    public Greeting home() {
        return new Greeting("Hello World!");
    }
}

class Greeting {

    private String id = UUID.randomUUID().toString();

    private String msg;

    @SuppressWarnings("unused")
    private Greeting() {
    }

    public Greeting(String msg) {
        this.msg = msg;
    }

    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Greeting [msg=" + msg + "]";
    }

}
