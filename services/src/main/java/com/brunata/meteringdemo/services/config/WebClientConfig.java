package com.brunata.meteringdemo.services.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    /**
     * Konfiguriert den globalen WebClient.Builder (Boot-Autoconfiguration) mit Timeouts und sinnvollen Defaults.
     */
    @Bean
    public WebClientCustomizer integrationWebClientCustomizer() {
        return builder -> {
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                    .responseTimeout(Duration.ofSeconds(5));

            builder.clientConnector(new ReactorClientHttpConnector(httpClient));

            builder.codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(256 * 1024));

            builder.defaultHeaders(h -> h.add("User-Agent", "metering-demo/0.1"));
        };
    }
}

