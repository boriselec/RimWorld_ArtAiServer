package com.boriselec.rimworld.aiart.generator;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpClient {
    public record GcpInstance(String project, String zone, String instance) {}

    private final GcpInstance instance;
    private final InstancesClient instancesClient;

    public GcpClient(GcpInstance instance, InstancesClient instancesClient) {
        this.instance = instance;
        this.instancesClient = instancesClient;
    }

    public Instance get() {
        return instancesClient.get(instance.project(), instance.zone(), instance.instance());
    }

    public void start() {
        instancesClient.startAsync(instance.project(), instance.zone(), instance.instance());
    }

    public void stop() {
        instancesClient.stopAsync(instance.project(), instance.zone(), instance.instance());
    }
}
