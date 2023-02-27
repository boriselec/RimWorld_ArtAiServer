package com.boriselec.rimworld.aiart.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;

@Component
public class StaticGenerator implements GeneratorClient {
    @Value( "${generator.url}" )
    private String url;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public InputStream getImage(String description) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                    .POST(HttpRequest.BodyPublishers.ofString(description))
                    .build();
            return httpClient
                    .send(request, this::processResponse)
                    .body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BodySubscriber<InputStream> processResponse(ResponseInfo responseInfo) {
        responseInfo.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                .filter(contentType -> MediaType.IMAGE_PNG.toString().equals(contentType))
                .orElseThrow();
        return BodySubscribers.ofInputStream();
    }
}
