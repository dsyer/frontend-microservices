package com.example;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template.Fragment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);
    }
}

@ControllerAdvice
class LayoutAdvice {

    private final Mustache.Compiler compiler;

    @Autowired
    public LayoutAdvice(Compiler compiler) {
        this.compiler = compiler;
    }

    @ModelAttribute("layout")
    public Mustache.Lambda layout(Map<String, Object> model) {
        return new Layout(compiler);
    }

    @ModelAttribute("script")
    public Mustache.Lambda script(@ModelAttribute Layout layout) {
        return (frag, out) -> {
            layout.script = frag.execute();
        };
    }
}

class Layout implements Mustache.Lambda {

    String title = "Demo Resource";

    String body;

    String script = "";

    private Compiler compiler;

    public Layout(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void execute(Fragment frag, Writer out) throws IOException {
        body = frag.execute();
        compiler.compile("{{>layout}}").execute(frag.context(), out);
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
