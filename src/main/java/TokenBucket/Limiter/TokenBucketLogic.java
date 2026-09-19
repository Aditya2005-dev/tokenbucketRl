package TokenBucket.Limiter;

public class TokenBucketLogic {

    private final int capacity;
    private double tokens;
    private final double refillRate;
    private long lastRefillTime;

    public TokenBucketLogic(int capacity, double refillRate) {

        this.capacity = capacity;
        this.tokens = capacity;
        this.refillRate = refillRate;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {

        refill();

        if (tokens >= 1) {
            tokens--;
            return true;
        }

        return false;
    }

    private void refill() {

        long currentTime = System.currentTimeMillis();

        long elapsedTime =
                currentTime - lastRefillTime;

        double newTokens =
                (elapsedTime / 1000.0) * refillRate;

        tokens =
                Math.min(capacity, tokens + newTokens);

        lastRefillTime = currentTime;
    }

    public synchronized double getRemainingTokens() {
        refill();
        return tokens;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getRefillRate() {
        return refillRate;
    }
}