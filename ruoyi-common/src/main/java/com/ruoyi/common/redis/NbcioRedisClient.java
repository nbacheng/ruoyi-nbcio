package com.ruoyi.common.redis;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.ruoyi.common.base.BaseMap;
import com.ruoyi.common.constant.GlobalConstants;

/**
 * redis客户端
 * 
 * @author nbacheng
 * @date 2023-09-20
 */
@Configuration
public class NbcioRedisClient {

	@Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 发送消息
     *
     * @param handlerName
     * @param params
     */
    public void sendMessage(String handlerName, BaseMap params) {
        params.put(GlobalConstants.HANDLER_NAME, handlerName);
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, params);
    }

}
