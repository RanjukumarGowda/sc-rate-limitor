package com.sc.rate.limitor.domain.tokenbucket;

import com.sc.rate.limitor.domain.dto.ClientInformation;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;

public class ClientTokenBucketProvider {

    public Bucket getClientBandwidth(ClientInformation clientInformation) {
        var timeoutDuration = getDurationFromTimeUnit(clientInformation.getUnit());
        var tokenRefill = Refill.intervally(clientInformation.getRequestLimit(), timeoutDuration);
        return bucket(Bandwidth.classic(clientInformation.getRequestLimit(), tokenRefill));
    }

    private Duration getDurationFromTimeUnit(String unit) {
        switch (unit) {
            case "Second":
                return Duration.ofSeconds(1);
            case "Hour":
                return Duration.ofHours(1);
            default:
                return Duration.ofMinutes(1);
        }
    }

    private Bucket bucket(Bandwidth limit) {
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
