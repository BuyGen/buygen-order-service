package com.project.microservices.order_service.config;

import com.project.microservices.order_service.client.InventoryClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;



import java.time.Duration;
@Configuration
public class RestClientConfig {

    private final String inventoryServiceUrl;
    private final ObservationRegistry observationRegistry;

    public RestClientConfig(
            @Value("${inventory.url}") String inventoryServiceUrl,
            ObservationRegistry observationRegistry
    ) {
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public InventoryClient inventoryClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(3))
                .setResponseTimeout(Timeout.ofSeconds(3))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestClient restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .requestFactory(requestFactory)
                .observationRegistry(observationRegistry)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(InventoryClient.class);
    }
}
