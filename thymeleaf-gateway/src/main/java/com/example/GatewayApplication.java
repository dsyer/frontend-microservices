package com.example;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.Application.Menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.ParsingPathMatcher;

@SpringBootApplication
public class GatewayApplication extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/login", "/error").permitAll()
                .antMatchers("/**").authenticated().and().exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

@Component
@ConfigurationProperties("app")
class Application {

    private List<Menu> menus = new ArrayList<>();

    public List<Menu> getMenus() {
        return menus;
    }

    public static class Menu {
        private String name;
        private String path;
        private String title;
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public Menu getMenu(String name) {
        for (Menu menu : menus) {
            if (menu.getName().equalsIgnoreCase(name)) {
                return menu;
            }
        }
        return menus.get(0);
    }
}

@ControllerAdvice
class LayoutAdvice {

    private Application application;

    @Autowired
    public LayoutAdvice(Application application) {
        this.application = application;
    }

    @ModelAttribute("menus")
    public Iterable<Menu> menus() {
        for (Menu menu : application.getMenus()) {
            menu.setActive(false);
        }
        return application.getMenus();
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
class ResourceController extends WebMvcConfigurerAdapter {
    private RestTemplate template;

    @Value("${app.services.resource}")
    private URI resourceUrl;

    ResourceController(RestTemplateBuilder builder) {
        template = builder.build();
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPathMatcher(new ParsingPathMatcher());
        configurer.setUseSuffixPatternMatch(false);
        configurer.setUseTrailingSlashMatch(false);
    }

    @GetMapping("/resource/{*path}")
    public byte[] resource(@PathVariable String path) throws Exception {
        if ("".equals(path)) {
            path = "/resource";
        }
        return template
                .exchange(RequestEntity.get(new URI(resourceUrl.toString() + path))
                        .accept(MediaType.APPLICATION_JSON).build(), byte[].class)
                .getBody();
    }
}

@Controller
@RequestMapping("/login")
class LoginController {

    private SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();

    @GetMapping
    public String form() {
        return "login";
    }

    @PostMapping
    public void authenticate(@RequestParam Map<String, String> map,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        Authentication result = new UsernamePasswordAuthenticationToken(
                map.get("username"), "N/A",
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(result);
        handler.onAuthenticationSuccess(request, response, result);
    }
}
