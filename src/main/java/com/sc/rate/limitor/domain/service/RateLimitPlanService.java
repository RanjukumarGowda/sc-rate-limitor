package com.sc.rate.limitor.domain.service;

import com.sc.rate.limitor.domain.dto.RateLimit;
import com.sc.rate.limitor.domain.exception.ClientIdNotFoundException;
import com.sc.rate.limitor.domain.tokenbucket.ClientTokenBucketProvider;
import io.github.bucket4j.Bucket;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class RateLimitPlanService {

    private final RateLimit rateLimit;
    private final ClientTokenBucketProvider clientBandwidthProvider;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String clientId, String uri) {
        var cacheKey = uri + clientId;
        return Option.of(cache.get(cacheKey))
                .getOrElse(() -> {
                    var newBucket = newBucket(clientId, uri);
                    cache.put(cacheKey, newBucket);
                    return cache.get(cacheKey);
                });
    }

    private Bucket newBucket(String clientId, String uri) {
        var optionalClientRateLimit = rateLimit.getRateLimitFilters().stream()
                .filter(it -> uri.equals(it.getUrl())).findFirst();

        if (optionalClientRateLimit.isPresent()) {
            var optionalClientInformation = optionalClientRateLimit.get().getClientInformation().stream()
                    .filter(it -> clientId.equals(it.getClientId())).findFirst();

            return clientBandwidthProvider.getClientBandwidth(optionalClientInformation
                    .orElseThrow(() -> new ClientIdNotFoundException("Unknown clientId", 401)));
        } else {
            throw new ClientIdNotFoundException("Unknown clientId", 401);
        }
    }
}
