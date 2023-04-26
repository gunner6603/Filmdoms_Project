package com.filmdoms.community;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Slf4j
@SpringBootApplication
public class CommunityApplication {

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext()
                .getEnvironment();

        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));

        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                //.filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
                .forEach(prop -> log.info("{}: {}", prop, env.getProperty(prop)));
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
