package com.springboot.shedlock.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty("shedlock.redis.host")
@Slf4j
public class CommonRedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties properties) {
        ClientOptions build = ClientOptions.builder()
                .autoReconnect(true)
                .timeoutOptions(TimeoutOptions.enabled(properties.getTimeout()))
                .build();

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientName("my-redis")
                .clientOptions(build)
                .build();
        RedisProperties.RedisMode mode = properties.getMode();
        switch (mode) {
            case CLUSTER:
                return createCluster(properties, clientConfiguration);
            case SENTINEL:
                return createSentinel(properties, clientConfiguration);
            case STANDALONE:
                return createStandAlone(properties, clientConfiguration);
            default:
                throw new IllegalArgumentException("redisMode[STANDALONE|SENTINEL|CLUSTER]:" + mode);
        }
    }

    private LettuceConnectionFactory createCluster(RedisProperties properties,
                                                   LettuceClientConfiguration clientConfiguration) {
        RedisClusterConfiguration conf = new RedisClusterConfiguration();
        String[] hosts = StringUtils.split(properties.getHost(), ",");
        String[] ports = StringUtils.split(properties.getPort(), ",");
        List<RedisNode> nodes = new ArrayList<>(hosts.length);
        for (int i = 0; i < hosts.length; i++) {
            nodes.add(new RedisClusterNode(hosts[i], Integer.parseInt(ports[i])));
        }
        conf.setClusterNodes(nodes);
        conf.setMaxRedirects(8);
        if (!StringUtils.isEmpty(properties.getPassword())) {
            conf.setPassword(properties.getPassword());
        }

        return new LettuceConnectionFactory(conf, clientConfiguration);
    }


    /**
     * 注意配置第一个节点为Master
     *
     * @return LettuceConnectionFactory
     */
    private LettuceConnectionFactory createSentinel(RedisProperties properties,
                                                    LettuceClientConfiguration clientConfiguration) {
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        String[] hosts = StringUtils.split(properties.getHost(), ",");
        String[] ports = StringUtils.split(properties.getPort(), ",");


        for (int i = 0; i < hosts.length; i++) {
            String host = hosts[i];
            String port = ports[i];
            RedisNode node = new RedisNode(host, Integer.parseInt(port));
            configuration.addSentinel(node);
        }
        configuration.master(properties.getMasterName());

        if (!StringUtils.isEmpty(properties.getPassword())) {
            configuration.setPassword(properties.getPassword());
        }
        return new LettuceConnectionFactory(configuration, clientConfiguration);
    }

    private LettuceConnectionFactory createStandAlone(RedisProperties properties,
                                                      LettuceClientConfiguration clientConfiguration) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        if (!StringUtils.isEmpty(properties.getPassword())) {
            configuration.setPassword(properties.getPassword());
        }
        configuration.setHostName(properties.getHost());
        configuration.setPort(Integer.parseInt(properties.getPort()));
        return new LettuceConnectionFactory(configuration, clientConfiguration);
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(factory);
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "strRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate();
        template.setConnectionFactory(factory);
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    private void setSerializer(RedisTemplate template) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = getJacksonSerializer();
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setKeySerializer(new StringRedisSerializer());
    }

    private Jackson2JsonRedisSerializer<Object> getJacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        om.registerModule(javaTimeModule);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }


}
