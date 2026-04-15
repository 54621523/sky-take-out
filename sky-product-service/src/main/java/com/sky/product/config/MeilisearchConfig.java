package com.sky.product.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "meilisearch")
public class MeilisearchConfig {

    private String hostUrl;
    private boolean enabled;

    @Bean
    public Client meilisearchClient() {
        Config config = new Config(hostUrl);
        return new Client(config);
    }
}
