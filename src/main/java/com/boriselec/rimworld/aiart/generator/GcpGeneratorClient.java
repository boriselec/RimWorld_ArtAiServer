package com.boriselec.rimworld.aiart.generator;

import com.google.cloud.compute.v1.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpGeneratorClient implements GeneratorClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationContext appCtx;
    private final GcpClient gcpClient;
    private final int stopAfterSeconds;
    private final int stopAfterFailedTries;

    private final AtomicReference<LocalDateTime> lastRequest = new AtomicReference<>(LocalDateTime.now());
    private final AtomicReference<Integer> unresponsiveCount = new AtomicReference<>(0);

    public GcpGeneratorClient(ApplicationContext appCtx, GcpClient gcpClient,
                              @Value("${gcp.idle.stopAfterSeconds}") int stopAfterSeconds,
                              @Value("${gcp.unresponsive.stopAfterFailedTries}") int stopAfterFailedTries) {
        this.appCtx = appCtx;
        this.gcpClient = gcpClient;
        this.stopAfterSeconds = stopAfterSeconds;
        this.stopAfterFailedTries = stopAfterFailedTries;
    }

    @Override
    public InputStream getImage(String description) throws GeneratorNotReadyException {
        lastRequest.set(LocalDateTime.now());
        try {
            InputStream image = getClient().getImage(description);
            unresponsiveCount.set(0);
            return image;
        } catch (GeneratorNotReadyException e) {
            throw e;
        } catch (Exception e) {
            int tryCount = unresponsiveCount.getAndUpdate(i -> i + 1);
            throw new GeneratorNotReadyException("GCP generator is running but not available yet." +
                    "try count: " + tryCount);
        }
    }

    private GeneratorClient getClient() throws GeneratorNotReadyException {
        Instance currentInstance = gcpClient.get();
        switch (currentInstance.getStatus()) {
            case "RUNNING":
                String ip = currentInstance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                String url = String.format("http://%s:8081/generate", ip);
                return appCtx.getBean(StaticGeneratorClient.class, url);
            case "TERMINATED":
                gcpClient.start();
            default:
                throw new GeneratorNotReadyException("GCP generator state: " + currentInstance.getStatus());
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void stopIfIdle() {
        boolean isIdle = LocalDateTime.now().minusSeconds(stopAfterSeconds).isAfter(lastRequest.get());
        boolean isUnresponsive = unresponsiveCount.get() > stopAfterFailedTries;
        if (isIdle || isUnresponsive) {
            Instance currentInstance = gcpClient.get();
            if (!"TERMINATED".equals(currentInstance.getStatus())) {
                log.info("Stopping instance");
                gcpClient.stop();
                unresponsiveCount.set(0);
            }
        }
    }
}
