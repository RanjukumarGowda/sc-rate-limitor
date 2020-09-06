package com.sc.rate.limitor.config;

import com.sc.rate.limitor.domain.dto.RateLimit;
import com.sc.rate.limitor.domain.interceptor.RateLimitInterceptor;
import com.sc.rate.limitor.domain.tokenbucket.ClientTokenBucketProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.yaml.snakeyaml.Yaml;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Lazy
    @Autowired
    private RateLimitInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/area/**");
    }

    @Bean
    public RateLimit rateLimit() {
        return new Yaml().loadAs(getClass().getResourceAsStream("/rate-limit-list.yaml"), RateLimit.class);
    }

    @Bean
    public ClientTokenBucketProvider clientBandwidthProvider() {
        return new ClientTokenBucketProvider();
    }
}
