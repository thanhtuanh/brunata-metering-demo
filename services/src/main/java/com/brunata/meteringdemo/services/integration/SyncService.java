package com.brunata.meteringdemo.services.integration;

import com.brunata.meteringdemo.persistence.DeviceRepository;
import com.brunata.meteringdemo.services.config.IntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Einfache Synchronisationslogik (Mock) für Jira/ERP, um Erfahrung mit REST, Datenflüssen
 * und Scheduling zu demonstrieren. In echten Projekten würden hier Auth, Retry, CircuitBreaker etc. dazukommen.
 */
@Service
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final DeviceRepository deviceRepo;
    private final WebClient webClient;
    private final IntegrationProperties props;

    public SyncService(DeviceRepository deviceRepo, WebClient.Builder builder, IntegrationProperties props) {
        this.deviceRepo = deviceRepo;
        this.props = props;
        this.webClient = builder.build();
    }

    /**
     * Meldet Geräte, die seit X Stunden offline sind, an ein Jira-Mock-Endpoint.
     * Läuft alle 5 Minuten. Fehler werden geloggt, aber nicht propagiert (best-effort).
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void reportOfflineDevicesToJira() {
        if (!props.enabled() || props.jiraBaseUrl() == null || props.jiraBaseUrl().isBlank()) return;
        var cutoff = Instant.now().minus(Duration.ofHours(props.offlineHours()));
        var offlineDevices = deviceRepo.findByLastSeenAtIsNullOrLastSeenAtBefore(cutoff);

        if (offlineDevices.isEmpty()) {
            log.info("Jira sync finished (cutoff={}, candidates=0)", cutoff);
            return;
        }

        var start = Instant.now();
        int concurrency = 4; // konservatives Limit; vermeidet Backpressure-Probleme
        Duration perRequestTimeout = Duration.ofSeconds(5);
        Duration totalBudget = Duration.ofSeconds(Math.min(15, offlineDevices.size() * 2L));

        Flux.fromIterable(offlineDevices)
                .flatMap(d -> {
                    var payload = Map.of(
                            "summary", "Device offline: " + d.getSerialNo(),
                            "deviceId", String.valueOf(d.getId()),
                            "lastSeenAt", String.valueOf(d.getLastSeenAt())
                    );
                    return webClient.post()
                            .uri(props.jiraBaseUrl() + "/mock/issue")
                            .bodyValue(payload)
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(perRequestTimeout)
                            .retryWhen(Retry.backoff(2, Duration.ofMillis(200)).filter(this::isRetryable))
                            .onErrorResume(ex -> {
                                log.warn("Jira sync failed for {}: {}", d.getSerialNo(), ex.toString());
                                return Mono.empty();
                            });
                }, concurrency)
                .collectList()
                .block(totalBudget);

        var took = Duration.between(start, Instant.now()).toMillis();
        log.info("Jira sync finished (cutoff={}, candidates={}, tookMs={})", cutoff, offlineDevices.size(), took);
    }

    /**
     * Dummy-ERP-Sync (nur Demonstration): ruft eine Liste von Kunden vom ERP-Mock ab und loggt die Anzahl.
     */
    @Scheduled(cron = "30 */10 * * * *")
    public void syncCustomersFromErp() {
        if (!props.enabled() || props.erpBaseUrl() == null || props.erpBaseUrl().isBlank()) return;
        try {
            var customers = webClient.get()
                    .uri(props.erpBaseUrl() + "/mock/customers")
                    .retrieve()
                    .bodyToMono(String[].class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn(new String[0])
                    .block();
            log.info("ERP sync finished (customers fetched={})", customers != null ? customers.length : 0);
        } catch (Exception ex) {
            log.warn("ERP sync failed: {}", ex.toString());
        }
    }

    private boolean isRetryable(Throwable ex) {
        if (ex instanceof WebClientResponseException we) {
            int s = we.getStatusCode().value();
            return s >= 500; // nur 5xx wiederholen; 4xx nicht
        }
        // Zeitüberschreitungen/Netzwerkfehler etc. sind idR transient
        return true;
    }
}
