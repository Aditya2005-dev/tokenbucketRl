package TokenBucket.Limiter;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class Controller {

    private final ProxyUrl proxyUrl;

    public Controller(ProxyUrl proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @PostMapping("/protect")
    public Map<String, String> createProtectedUrl(
            @RequestBody Model model) {

        String proxyId =
                proxyUrl.createProxy(model);

        String protectedUrl =
                "http://localhost:8080/proxy/" + proxyId;

        return Map.of(
                "protectedUrl", protectedUrl
        );
    }
}