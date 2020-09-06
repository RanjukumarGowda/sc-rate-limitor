package com.sc.rate.limitor.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimit {

    private List<Filter> rateLimitFilters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String url;
        private List<ClientInformation> clientInformation;
    }
}
