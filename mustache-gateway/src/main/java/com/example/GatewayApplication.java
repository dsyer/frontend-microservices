package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.Application.Menu;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template.Fragment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    private final Mustache.Compiler compiler;

    private Application application;

    @Autowired
    public LayoutAdvice(Compiler compiler, Application application) {
        this.compiler = compiler;
        this.application = application;
    }

    @ModelAttribute("menus")
    public Iterable<Menu> menus(@ModelAttribute Layout layout) {
        for (Menu menu : application.getMenus()) {
            menu.setActive(false);
        }
        return application.getMenus();
    }

    @ModelAttribute("menu")
    public Mustache.Lambda menu(@ModelAttribute Layout layout) {
        return (frag, out) -> {
            Menu menu = application.getMenu(frag.execute());
            menu.setActive(true);
            layout.title = menu.getTitle();
        };
    }

    @ModelAttribute("script")
    public Mustache.Lambda script(@ModelAttribute Layout layout) {
        return (frag, out) -> {
            layout.script = frag.execute();
        };
    }

    @ModelAttribute("layout")
    public Mustache.Lambda layout(Map<String, Object> model) {
        return new Layout(compiler);
    }
}

class Layout implements Mustache.Lambda {

    String title = "Demo Application";

    StringBuilder body = new StringBuilder();

    String script = "";

    int depth;

    private Compiler compiler;

    public Layout(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void execute(Fragment frag, Writer out) throws IOException {
        depth++;
        body.append(frag.execute());
        depth--;
        if (depth == 0) {
            compiler.compile("{{>layout}}").execute(frag.context(), out);
        }
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
    private RestTemplate template;

    @Value("${app.services.resource}")
    private URI resourceUrl;

    ResourceController(RestTemplateBuilder builder) {
        template = builder.build();
    }

    @GetMapping
    public Map<String, String> resource() throws Exception {
        return template.exchange(
                RequestEntity.get(new URI(resourceUrl.toString() + "/resource"))
                        .accept(MediaType.APPLICATION_JSON).build(),
                new ParameterizedTypeReference<Map<String, String>>() {
                }).getBody();
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

@Configuration
@ConfigurationProperties("app")
class MustacheConfguration {

    private final MustacheProperties mustache;

    private Map<String,URI> services = new LinkedHashMap<>();

    public MustacheConfguration(MustacheProperties mustache) {
        this.mustache = mustache;
    }

    public Map<String, URI> getServices() {
        return services;
    }

    @Bean
    public TemplateLoader mustacheTemplateLoader() {
        MustacheResourceTemplateLoader loader = new MustacheResourceTemplateLoader(
                this.mustache.getPrefix(), this.mustache.getSuffix());
        loader.setCharset(this.mustache.getCharsetName());
        return new CompositeTemplateLoader(
                Arrays.asList(new RemoteTemplateLoader(this.services), loader));
    }

}

class RemoteTemplateLoader implements TemplateLoader {

    private Map<String,URI> urls;

    public RemoteTemplateLoader(Map<String,URI> urls) {
        this.urls = urls;
    }

    @Override
    public Reader getTemplate(String name) throws Exception {
        if (!name.contains(":")) {
            return null;
        }
        String service = name.substring(0, name.indexOf(":"));
        name = name.substring(name.indexOf(":")+1);
        URI uri = urls.get(service);
        if (uri==null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(
                new URL(uri.toString() + "/templates/" + name + ".html")
                        .openStream()));
    }

}

class CompositeTemplateLoader implements TemplateLoader {

    private final List<TemplateLoader> delegates;

    public CompositeTemplateLoader(List<TemplateLoader> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Reader getTemplate(String name) throws Exception {
        for (TemplateLoader loader : delegates) {
            try {
                Reader template = loader.getTemplate(name);
                if (template != null) {
                    return template;
                }
            }
            catch (Exception e) {
            }
        }
        throw new IllegalStateException("Cannot find template: " + name);
    }

}