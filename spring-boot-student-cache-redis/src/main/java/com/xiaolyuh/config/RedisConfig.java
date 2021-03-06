package com.xiaolyuh.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfig {

	// redis缓存的有效时间单位是秒
	@Value("${redis.default.expiration:3600}")
	private long redisDefaultExpiration;

	/**
	 * 重写Redis序列化方式，使用Json方式:
	 * 当我们的数据存储到Redis的时候，我们的键（key）和值（value）都是通过Spring提供的Serializer序列化到数据库的。RedisTemplate默认使用的是JdkSerializationRedisSerializer，StringRedisTemplate默认使用的是StringRedisSerializer。
	 * Spring Data JPA为我们提供了下面的Serializer：
	 * GenericToStringSerializer、Jackson2JsonRedisSerializer、JacksonJsonRedisSerializer、JdkSerializationRedisSerializer、OxmSerializer、StringRedisSerializer。
	 * 在此我们将自己配置RedisTemplate并定义Serializer。
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);  
        jackson2JsonRedisSerializer.setObjectMapper(om);

		// 设置值（value）的序列化采用Jackson2JsonRedisSerializer。
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		// 设置键（key）的序列化采用StringRedisSerializer。
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	/**
	 * 通过CacheManager设置缓存的有效时间
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public RedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
		RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
		redisCacheManager.setUsePrefix(true);
		//这里可以设置一个默认的过期时间 单位是秒
		redisCacheManager.setDefaultExpiration(redisDefaultExpiration);
		//针对各各key设置一个过期时间 单位是秒
		Map<String, Long> expires = new HashMap<String, Long>();
		expires.put("person2", 1000L);
		redisCacheManager.setExpires(expires);
		return redisCacheManager;
	}

}
