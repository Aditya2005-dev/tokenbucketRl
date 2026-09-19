package TokenBucket.Limiter;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProxyUrl {

    private final Map<String, String> urlMap =
            new ConcurrentHashMap<>();

    private final Map<String, TokenBucketLogic> bucketMap =
            new ConcurrentHashMap<>();

    public String createProxy(Model model) {

        String proxyId =
                UUID.randomUUID().toString();

        TokenBucketLogic bucket =
                new TokenBucketLogic(
                        model.getCapacity(),
                        model.getRefillRate()
                );

        urlMap.put(
                proxyId,
                model.getUrl()
        );

        bucketMap.put(
                proxyId,
                bucket
        );

        return proxyId;
    }

    public String getOriginalUrl(String proxyId) {
        return urlMap.get(proxyId);
    }

    public TokenBucketLogic getBucket(String proxyId) {
        return bucketMap.get(proxyId);
    }
}