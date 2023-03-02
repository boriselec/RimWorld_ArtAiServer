package com.boriselec.rimworld.aiart.generator;

import org.springframework.http.HttpHeaders;
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

public class StaticGeneratorClient implements GeneratorClient {
    private final String url;
    private final HttpClient httpClient;

    public StaticGeneratorClient(String url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public InputStream getImage(String description) throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Getting image...");

        HttpRequest request = HttpRequest.newBuilder(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(description))
                .build();
        InputStream response = httpClient
                .send(request, this::processResponse)
                .body();

        System.out.println("Getting image: DONE");
        return response;
    }

    private BodySubscriber<InputStream> processResponse(ResponseInfo responseInfo) {
        responseInfo.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                .filter(contentType -> MediaType.IMAGE_PNG.toString().equals(contentType))
                .orElseThrow();
        return BodySubscribers.ofInputStream();
    }
}
