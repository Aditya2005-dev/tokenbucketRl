package TokenBucket.Limiter;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@CrossOrigin("*")
public class ProxyController {

    private final ProxyUrl proxyUrl;

    private final RestTemplate restTemplate =
            new RestTemplate();

    public ProxyController(ProxyUrl proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @RequestMapping(
            value = "/proxy/{proxyId}",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.DELETE
            }
    )
    public ResponseEntity<String> proxyRequest(
            @PathVariable String proxyId,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers,
            HttpMethod method) {

        TokenBucketLogic bucket =
                proxyUrl.getBucket(proxyId);

        if (bucket == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Protected URL not found");
        }

        // Check token bucket
        if (!bucket.allowRequest()) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(
                            "X-RateLimit-Limit",
                            String.valueOf(
                                    bucket.getCapacity()
                            )
                    )
                    .header(
                            "X-RateLimit-Remaining",
                            "0"
                    )
                    .body("Rate limit exceeded");
        }

        double remainingTokens =
                bucket.getRemainingTokens();

        String originalUrl =
                proxyUrl.getOriginalUrl(proxyId);

        try {

            HttpHeaders requestHeaders =
                    new HttpHeaders();

            headers.forEach(
                    (key, value) ->
                            requestHeaders.add(key, value)
            );

            HttpEntity<String> request =
                    new HttpEntity<>(
                            body,
                            requestHeaders
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            originalUrl,
                            method,
                            request,
                            String.class
                    );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .header(
                            "X-RateLimit-Limit",
                            String.valueOf(
                                    bucket.getCapacity()
                            )
                    )
                    .header(
                            "X-RateLimit-Remaining",
                            String.valueOf(
                                    (int) remainingTokens
                            )
                    )
                    .body(response.getBody());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(
                            "Unable to reach original API"
                    );
        }
    }

    @GetMapping("/proxy/{proxyId}/status")
    public ResponseEntity<?> getStatus(
            @PathVariable String proxyId) {

        TokenBucketLogic bucket =
                proxyUrl.getBucket(proxyId);

        if (bucket == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Protected URL not found");
        }

        return ResponseEntity.ok(
                Map.of(
                        "tokens",
                        bucket.getRemainingTokens(),

                        "capacity",
                        bucket.getCapacity(),

                        "refillRate",
                        bucket.getRefillRate()
                )
        );
    }
}