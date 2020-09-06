package com.sc.rate.limitor.domain.interceptor;

import com.sc.rate.limitor.domain.exception.ClientIdNotFoundException;
import com.sc.rate.limitor.domain.service.RateLimitPlanService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import static com.sc.rate.limitor.domain.interceptor.RateLimitInterceptor.HEADER_CLIENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitInterceptorTest {

    public static final String TEST_URL = "/test";

    @Mock
    private RateLimitPlanService rateLimitPlanServiceMock;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    public void whenClientIdAbsentInRequestHeader_shouldBreakTheHttpRequestChain() throws IOException {
        var request = new MockHttpServletRequest();
        request.setRequestURI(TEST_URL);
        var response = new MockHttpServletResponse();

        var isProceed = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(isProceed).isFalse();
        verifyRateLimitPlanServiceCallsWith(0);
    }

    @Test
    public void whenClientIdNotFoundInRateLimitList_shouldBreakTheHttpRequestChain() throws IOException {
        when(rateLimitPlanServiceMock.resolveBucket(any(), any())).thenThrow(new ClientIdNotFoundException("Not Found", 401));
        var request = getHttpRequest();
        var response = new MockHttpServletResponse();

        var isProceed = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(isProceed).isFalse();
        verifyRateLimitPlanServiceCallsWith(1);
    }

    @Test
    public void whenClientExceedsRateLimit_shouldBreakHttpRequestChain() throws IOException {
        var testBucket = Bucket4j.builder().addLimit(Bandwidth.simple(1, Duration.ofSeconds(1))).build();
        when(rateLimitPlanServiceMock.resolveBucket(any(), any())).thenReturn(testBucket);
        var request = getHttpRequest();
        var response = new MockHttpServletResponse();

        rateLimitInterceptor.preHandle(request, response, new Object());
        var isProceed = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(isProceed).isFalse();
        verifyRateLimitPlanServiceCallsWith(2);
    }

    @Test
    public void whenUnknownException_shouldBreakHttpRequestChain() throws IOException {
        when(rateLimitPlanServiceMock.resolveBucket(any(), any())).thenThrow(new RuntimeException());
        var request = getHttpRequest();
        var response = new MockHttpServletResponse();

        var isProceed = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(isProceed).isFalse();
        verifyRateLimitPlanServiceCallsWith(1);
    }

    @Test
    public void whenRequestIsWithInRange_shouldContinueHttpRequestChain() throws IOException {
        var testBucket = Bucket4j.builder().addLimit(Bandwidth.simple(1, Duration.ofSeconds(1))).build();
        when(rateLimitPlanServiceMock.resolveBucket(any(), any())).thenReturn(testBucket);
        var request = getHttpRequest();
        var response = new MockHttpServletResponse();

        var isProceed = rateLimitInterceptor.preHandle(request, response, new Object());

        assertThat(isProceed).isTrue();
        verifyRateLimitPlanServiceCallsWith(1);
    }

    private MockHttpServletRequest getHttpRequest() {
        var request = new MockHttpServletRequest();
        request.setRequestURI(TEST_URL);
        request.addHeader(HEADER_CLIENT_ID, "1");
        return request;
    }

    private void verifyRateLimitPlanServiceCallsWith(int numberOfTimes) {
        verify(rateLimitPlanServiceMock, times(numberOfTimes)).resolveBucket(any(), any());
    }
}
