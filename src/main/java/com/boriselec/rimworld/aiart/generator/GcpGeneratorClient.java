package com.boriselec.rimworld.aiart.generator;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class GcpGeneratorClient implements GeneratorClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final InstancesClient instancesClient;
    private final String project;
    private final String zone;
    private final String instance;

    private final AtomicReference<LocalDateTime> lastRequest = new AtomicReference<>(LocalDateTime.now());

    public GcpGeneratorClient(String keyPath, String project, String zone, String instance) throws IOException {
        Credentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(keyPath));
        CredentialsProvider provider = FixedCredentialsProvider.create(credentials);
        InstancesSettings settings = InstancesSettings.newBuilder()
                .setCredentialsProvider(provider)
                .build();
        this.instancesClient = InstancesClient.create(settings);
        this.project = project;
        this.zone = zone;
        this.instance = instance;
    }

    @Override
    public InputStream getImage(String description) throws IOException, URISyntaxException, InterruptedException {
        lastRequest.set(LocalDateTime.now());
        try {
            return getClient().getImage(description);
        } catch (ConnectException e) {
            throw new GeneratorNotReadyException("GCP generator is running but not available yet");
        }
    }

    private GeneratorClient getClient() throws GeneratorNotReadyException {
        Instance currentInstance = instancesClient.get(project, zone, instance);
        switch (currentInstance.getStatus()) {
            case "RUNNING":
                String ip = currentInstance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                String url = String.format("http://%s:8081/generate", ip);
                return new StaticGeneratorClient(url);
            case "TERMINATED":
                instancesClient.startAsync(project, zone, instance);
            default:
                throw new GeneratorNotReadyException("GCP generator state: " + currentInstance.getStatus());
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void stopIfIdle() {
        boolean isIdle = LocalDateTime.now().minusMinutes(3).isAfter(lastRequest.get());
        if (isIdle) {
            Instance currentInstance = instancesClient.get(project, zone, instance);
            if (!"TERMINATED".equals(currentInstance.getStatus())) {
                log.info("Stopping instance");
                instancesClient.stopAsync(project, zone, instance);
            }
        }
    }
}
