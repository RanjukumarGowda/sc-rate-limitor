package com.sc.rate.limitor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static com.sc.rate.limitor.domain.interceptor.RateLimitInterceptor.HEADER_CLIENT_ID;
import static com.sc.rate.limitor.domain.interceptor.RateLimitInterceptor.HEADER_LIMIT_REMAINING;
import static com.sc.rate.limitor.domain.interceptor.RateLimitInterceptor.HEADER_RETRY_AFTER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RateLimiterApplication.class)
public class RateLimiterApplicationIT {

    public static final String RECTANGLE_END_POINT = "/api/v1/area/rectangle";
    public static final String TRIANGLE_END_POINT = "/api/v1/area/triangle";
    public static final String RECTANGLE_REQUEST_BODY = "{ \"length\": 12, \"width\": 10 }";
    public static final String TRIANGLE_REQUEST_BODY = "{ \"base\": 12, \"height\": 10 }";

    @Autowired
    private MockMvc mockMvc;

    //Rectangle endpoint
    @Test
    public void givenRectangleAreaCalculator_whenRequestsWithinRateLimit_thenAccepted() throws Exception {
        var request = getRequestBuilderFor(RECTANGLE_END_POINT, RECTANGLE_REQUEST_BODY, "1");

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HEADER_LIMIT_REMAINING))
                    .andExpect(jsonPath("$.shape", equalTo("rectangle")))
                    .andExpect(jsonPath("$.area", equalTo(120d)));
        }
    }

    @Test
    public void givenRectangleAreaCalculator_whenRequestRateLimitTriggered_thenRejected() throws Exception {
        var request = getRequestBuilderFor(RECTANGLE_END_POINT, RECTANGLE_REQUEST_BODY, "2");

        for (int i = 1; i <= 21; i++) {
            mockMvc.perform(request); // exhaust limit
        }

        mockMvc.perform(request)
                .andExpect(status().isTooManyRequests())
                .andExpect(status().reason("You have exhausted your API Request Quota"))
                .andExpect(header().exists(HEADER_RETRY_AFTER));
    }

    @Test
    public void givenRectangleAreaCalculator_whenClientIdNotFoundInAccessLimitList_thenRejected() throws Exception {
        var request = getRequestBuilderFor(RECTANGLE_END_POINT, RECTANGLE_REQUEST_BODY, "10");

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("ClientId not found"));
    }

    //Triangle endpoint
    @Test
    public void givenTriangleAreaCalculator_whenRequestsWithinRateLimit_thenAccepted() throws Exception {
        var request = getRequestBuilderFor(TRIANGLE_END_POINT, TRIANGLE_REQUEST_BODY, "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_LIMIT_REMAINING))
                .andExpect(jsonPath("$.shape", equalTo("triangle")))
                .andExpect(jsonPath("$.area", equalTo(60d)));
    }

    @Test
    public void givenTriangleAreaCalculator_whenRequestRateLimitTriggered_thenRejected() throws Exception {
        var request = getRequestBuilderFor(TRIANGLE_END_POINT, TRIANGLE_REQUEST_BODY, "2");

        for (int i = 1; i <= 21; i++) {
            mockMvc.perform(request); // exhaust limit
        }

        mockMvc.perform(request)
                .andExpect(status().isTooManyRequests())
                .andExpect(status().reason("You have exhausted your API Request Quota"))
                .andExpect(header().exists(HEADER_RETRY_AFTER));
    }

    @Test
    public void givenTriangleAreaCalculator_whenClientIdNotFoundInAccessLimitList_thenRejected() throws Exception {
        var request = getRequestBuilderFor(TRIANGLE_END_POINT, TRIANGLE_REQUEST_BODY, "10");

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("ClientId not found"));
    }

    private RequestBuilder getRequestBuilderFor(String url, String content, String clientId) {
        return post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content)
                .header(HEADER_CLIENT_ID, clientId);
    }
}
