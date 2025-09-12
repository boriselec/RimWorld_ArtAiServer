package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GcpClient;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.StaticGeneratorClient;
import com.boriselec.rimworld.aiart.translate.Translator;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SpringBootApplication
@EnableScheduling
public class Application implements SchedulingConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public LinkedBlockingDeque<Request> linkedBlockingQueue(MeterRegistry meterRegistry,
                                                            @Value("${queue.size.max}") int queueMaxSize) {
        var queue = new LinkedBlockingDeque<Request>(queueMaxSize);
        return meterRegistry.gauge("queue.size", queue, LinkedBlockingDeque::size);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public GcpClient.GcpInstance gcpInstance(@Value("${gcp.project}") String project,
                                             @Value("${gcp.zone}") String zone,
                                             @Value("${gcp.instance}") String instance) {
        return new GcpClient.GcpInstance(project, zone, instance);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public CredentialsProvider gcpCredentials(@Value("${gcp.key.path}") String keyPath) throws IOException {
        Credentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(keyPath));
        return FixedCredentialsProvider.create(credentials);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public InstancesClient instancesClient(CredentialsProvider provider) throws IOException {
        InstancesSettings settings = InstancesSettings.newBuilder()
                .setCredentialsProvider(provider)
                .build();
        return InstancesClient.create(settings);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public TranslationServiceClient translationServiceClient(CredentialsProvider provider) throws IOException {
        TranslationServiceSettings settings = TranslationServiceSettings.newBuilder()
                .setCredentialsProvider(provider)
                .build();
        return TranslationServiceClient.create(settings);
    }

    @Bean
    @ConditionalOnMissingBean(Translator.class)
    public Translator mockTranslator() {
        return (language, description) -> description;
    }

    @Bean
    @ConditionalOnProperty("generator.url")
    public GeneratorClient staticGeneratorClient(
            @Value("${generator.url}") String url,
            @Value("${generator.timeout.seconds}") int timeoutSeconds) {
        return staticGeneratorClientPrototype(url, timeoutSeconds);
    }

    @Bean(autowireCandidate = false)
    @Scope(SCOPE_PROTOTYPE)
    public StaticGeneratorClient staticGeneratorClientPrototype(
            String url,
            @Value("${generator.timeout.seconds}") int timeoutSeconds) {
        return new StaticGeneratorClient(url, timeoutSeconds);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Counters counters(MeterRegistry registry) {
        return new Counters(registry);
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean
    public Map<String, String> thingDescCache(MeterRegistry meterRegistry) {
        var cache = new ConcurrentHashMap<String, String>();
        return meterRegistry.gauge("thingDescCache.size", cache, ConcurrentHashMap::size);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }
}
