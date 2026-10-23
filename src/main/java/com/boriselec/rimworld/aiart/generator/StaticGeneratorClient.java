package com.boriselec.rimworld.aiart.generator;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.github.mizosoft.methanol.MoreBodyHandlers.withReadTimeout;

public class StaticGeneratorClient implements GeneratorClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String url;
    private final Duration timeout;
    private final HttpClient httpClient;

    public StaticGeneratorClient(String url, Duration timeout) {
        this.url = url;
        this.timeout = timeout;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    @Timed("generation.time")
    public InputStream getImage(Model model, String prompt)
        throws IOException, InterruptedException {
        log.info("Getting image (%s)...".formatted(model));

        URI uri = UriComponentsBuilder.fromUriString(url)
            .pathSegment(model.name(), "generate")
            .build().toUri();
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .POST(ofString(prompt))
            .build();
        HttpResponse<InputStream> response = httpClient.send(request,
            // TODO: drop methanol after JDK 26+ JDK-8208693
            withReadTimeout(ofInputStream(), timeout));

        HttpStatusCode statusCode = HttpStatusCode.valueOf(response.statusCode());
        if (statusCode != HttpStatus.OK) {
            response.body().close();
            String message = "HTTP code: " + statusCode;
            throw switch (statusCode.value()) {
                case 400, 413, 422 -> new GeneratorRejectedException(message);
                default -> new IllegalStateException(message);
            };
        }
        String contentType = response.headers()
            .firstValue(HttpHeaders.CONTENT_TYPE)
            .orElse(null);
        if (!MediaType.IMAGE_PNG.toString().equals(contentType)) {
            response.body().close();
            throw new IllegalStateException("Content-type: " + contentType);
        }

        log.info("Getting image: DONE");
        return response.body();
    }
}
