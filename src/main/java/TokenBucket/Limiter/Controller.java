package TokenBucket.Limiter;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createProtectedUrl(
            @RequestBody Model model) {

        if (model.getUrl() == null ||
                model.getUrl().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("URL is required");
        }

        if (model.getCapacity() <= 0) {

            return ResponseEntity
                    .badRequest()
                    .body("Capacity must be greater than 0");
        }

        if (model.getRefillRate() < 0) {

            return ResponseEntity
                    .badRequest()
                    .body("Refill rate cannot be negative");
        }

        String proxyId =
                proxyUrl.createProxy(model);

        String protectedUrl =
                "http://localhost:8080/proxy/" + proxyId;

        return ResponseEntity.ok(
                Map.of(
                        "protectedUrl", protectedUrl,
                        "capacity", model.getCapacity(),
                        "refillRate", model.getRefillRate()
                )
        );
    }
}