package com.boriselec.rimworld.aiart.generator;

import com.github.mizosoft.methanol.MoreBodySubscribers;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.time.Duration;

public class StaticGeneratorClient implements GeneratorClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String url;
    private final HttpClient httpClient;

    public StaticGeneratorClient(String url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    @Timed("generation.time")
    public InputStream getImage(String description) throws IOException, InterruptedException, URISyntaxException {
        log.info("Getting image...");

        HttpRequest request = HttpRequest.newBuilder(new URI(url))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(description))
                .build();
        InputStream response = httpClient
                .send(request, this::processResponse)
                .body();

        log.info("Getting image: DONE");
        return response;
    }

    private BodySubscriber<InputStream> processResponse(ResponseInfo responseInfo) {
        int statusCode = responseInfo.statusCode();
        if (statusCode != HttpStatus.OK.value()) {
            throw new IllegalStateException("HTTP code: " + statusCode);
        }
        String contentType = responseInfo.headers()
                .firstValue(HttpHeaders.CONTENT_TYPE)
                .orElse(null);
        if (!MediaType.IMAGE_PNG.toString().equals(contentType)) {
            throw new IllegalStateException("Content-type: " + contentType);
        }
        return MoreBodySubscribers.withReadTimeout(BodySubscribers.ofInputStream(), TIMEOUT);
    }
}
