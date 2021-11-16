package com.example;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class ResourceApplication {

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

@Controller
@RequestMapping("/message")
class ResourceController {
    @GetMapping
    public String home(Greeting greeting) {
        greeting.setMsg("Hello World!");
        return "message :: message";
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
