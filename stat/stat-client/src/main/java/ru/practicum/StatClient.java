package ru.practicum;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class StatClient {

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;
    private final RestClient restClient;

    public StatClient(DiscoveryClient discoveryClient, RetryTemplate retryTemplate, String statsServiceId) {
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
        this.statsServiceId = statsServiceId;
        this.restClient = RestClient.builder()
                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .defaultStatusHandler(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            throw new RestClientException("HTTP error: " + response.getStatusText());
                        })
                .build();
    }

    private ServiceInstance getInstance() {
        return Optional.ofNullable(discoveryClient.getInstances(statsServiceId))
                .filter(list -> !list.isEmpty())
                .map(List::getFirst)
                .orElseThrow(() -> new IllegalStateException("Stats server not available: " + statsServiceId));
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(context -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public void saveStatEvent(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri(makeUri("/hit"))
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(String start,
                                                       String end,
                                                       List<String> uris,
                                                       boolean unique) {
        String uri = buildStatsUri(start, end, uris, unique);
        return restClient.get()
                .uri(makeUri(uri))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    private String buildStatsUri(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        return builder.build().toUriString();
    }
}