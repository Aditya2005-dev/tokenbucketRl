package TokenBucket.Limiter;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin("*")
public class ProxyController {

    private final ProxyUrl proxyUrl;

    private final RestTemplate restTemplate =
            new RestTemplate();

    public ProxyController(ProxyUrl proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @GetMapping("/proxy/{proxyId}")
    public ResponseEntity<String> proxyRequest(
            @PathVariable String proxyId) {

        TokenBucketLogic bucket =
                proxyUrl.getBucket(proxyId);

        if (bucket == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Protected URL not found");
        }

        // Token Bucket check
        if (!bucket.allowRequest()) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        String originalUrl =
                proxyUrl.getOriginalUrl(proxyId);

        try {

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            originalUrl,
                            String.class
                    );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Unable to reach original API");
        }
    }

    @GetMapping("/proxy/{proxyId}/status")
    public ResponseEntity<?> getStatus(
            @PathVariable String proxyId) {

        TokenBucketLogic bucket =
                proxyUrl.getBucket(proxyId);

        if (bucket == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "tokens",
                        bucket.getTokens()
                )
        );
    }
}