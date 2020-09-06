package com.sc.rate.limitor.domain.service;

import com.sc.rate.limitor.domain.dto.ClientInformation;
import com.sc.rate.limitor.domain.dto.RateLimit;
import com.sc.rate.limitor.domain.exception.ClientIdNotFoundException;
import com.sc.rate.limitor.domain.tokenbucket.ClientTokenBucketProvider;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitPlanServiceTest {

    @Mock
    private ClientTokenBucketProvider clientBandwidthProvider;

    private RateLimitPlanService rateLimitPlanService;

    @Before
    public void setup() {
        var rateLimitList = buildTestRateLimitList();
        rateLimitPlanService = new RateLimitPlanService(rateLimitList, clientBandwidthProvider);
    }

    @Test
    public void whenClientIdNotFoundInRateLimitList_shouldThrowClientIdNotFoundException() {
        assertThatThrownBy(() -> rateLimitPlanService.resolveBucket("3", "/v1/test1"))
                .isInstanceOf(ClientIdNotFoundException.class)
                .hasMessage("message : Unknown clientId , statusCode: 401");
        verify(clientBandwidthProvider, times(0)).getClientBandwidth(any());
    }

    @Test
    public void whenValidClientId_shouldReturnValidTokenBucket() {
       var testBucket = Bucket4j.builder().addLimit(Bandwidth.simple(10, Duration.ofSeconds(1))).build();
        when(clientBandwidthProvider.getClientBandwidth(any())).thenReturn(testBucket);
        rateLimitPlanService.resolveBucket("1", "/v1/test1");
        verify(clientBandwidthProvider, times(1)).getClientBandwidth(any());

    }

    public RateLimit buildTestRateLimitList() {
        var clientInformation1 = ClientInformation.builder()
                .clientId("1").requestLimit(10).unit("Second").build();
        var clientInformation2 = ClientInformation.builder()
                .clientId("2").requestLimit(20).unit("Hour").build();

        var filter1 = RateLimit.Filter.builder()
                .url("/v1/test1").clientInformation(List.of(clientInformation1, clientInformation2)).build();
        var filter2 = RateLimit.Filter.builder()
                .url("/v1/test2").clientInformation(List.of(clientInformation1, clientInformation2)).build();

        return RateLimit.builder().rateLimitFilters(List.of(filter1, filter2)).build();
    }
}
