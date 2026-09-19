package TokenBucket.Limiter;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    private final ProxyUrl proxyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public ProxyController(ProxyUrl proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @GetMapping("/proxy/{proxyId}")
    public ResponseEntity<String> handleRequest(
            @PathVariable String proxyId) {

        TokenBucketLogic bucket =
                proxyUrl.getBucket(proxyId);

        if (bucket == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Proxy URL not found");
        }

        // Rate-limit check
        if (!bucket.allowRequest()) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        // Get original URL
        String originalUrl = proxyUrl.getUrl(proxyId);

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
                    .body("Unable to reach target API");
        }
    }

    @PostMapping("/proxy/{proxyId}")
public ResponseEntity<String> handlePostRequest(
        @PathVariable String proxyId,
        @RequestBody String requestBody) {

    TokenBucketLogic bucket =
            proxyUrl.getBucket(proxyId);

    if (bucket == null) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Proxy URL not found");
    }

    // Check rate limit
    if (!bucket.allowRequest()) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Rate limit exceeded");
    }

    // Get original URL
    String originalUrl = proxyUrl.getUrl(proxyId);

    try {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        originalUrl,
                        request,
                        String.class
                );

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());

    } catch (Exception e) {

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body("Unable to reach target API");
    }
    }
}