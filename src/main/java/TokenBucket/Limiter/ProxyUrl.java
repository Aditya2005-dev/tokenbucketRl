package TokenBucket.Limiter;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProxyUrl {

    private final Map<String, String> urlMap = new HashMap<>();
    private final Map<String, TokenBucketLogic> bucketMap = new HashMap<>();

    @PostMapping("/protect")
    public String createProxy(@RequestBody Model model) {

        String proxyId = UUID.randomUUID().toString();

        urlMap.put(proxyId, model.getUrl());

        TokenBucketLogic bucket =
                new TokenBucketLogic(
                        model.getCapacity(),
                        model.getRefillRate()
                );

        bucketMap.put(proxyId, bucket);

        return "http://localhost:8080/proxy/" + proxyId;
    }

    public String getUrl(String proxyId) {
        return urlMap.get(proxyId);
    }

    public TokenBucketLogic getBucket(String proxyId) {
        return bucketMap.get(proxyId);
    }
}