package com.sc.rate.limitor.domain.interceptor;

import com.sc.rate.limitor.domain.exception.ClientIdNotFoundException;
import com.sc.rate.limitor.domain.service.RateLimitPlanService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    public static final String HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    public static final String HEADER_RETRY_AFTER = "X-Rate-Limit-Retry-After-Seconds";

    @Autowired
    private RateLimitPlanService rateLimitPlanService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String clientId = request.getHeader(HEADER_CLIENT_ID);
        var uri = request.getRequestURI();

        log.debug("ClientId : {} request for URI : {} checking rate limit", clientId, uri);

        if (clientId == null || clientId.isEmpty()) {
            log.error("Missing Header: {}", HEADER_CLIENT_ID);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: " + HEADER_CLIENT_ID);
            return false;
        }

        try {
            return isRequestWithInRateLimit(clientId, uri, response);
        } catch (ClientIdNotFoundException ex) {
            log.error("ClientId : {} is not whitelisted for URI {}", clientId, uri);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "ClientId not found");
            return false;
        } catch (Exception ex) {
            log.error("Internal server error, error message : {}", ex.getMessage());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
            return false;
        }
    }

    private boolean isRequestWithInRateLimit(String clientId, String uri, HttpServletResponse response) throws IOException {
        Bucket tokenBucket = rateLimitPlanService.resolveBucket(clientId, uri);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.info("ClientId : {} request for URI : {} is within rate limit hence proceeding request", clientId, uri);
            response.addHeader(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.error("ClientId : {} request for URI : {} is exceeded rate limit should try after {} seconds", clientId, uri, waitForRefill);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HEADER_RETRY_AFTER, String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota");
            return false;
        }
    }
}