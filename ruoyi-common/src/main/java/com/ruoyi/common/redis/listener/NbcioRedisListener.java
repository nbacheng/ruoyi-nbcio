package com.ruoyi.common.redis.listener;

import com.ruoyi.common.base.BaseMap;


/**
 *  自定义消息监听
 *  @author nbacheng
 *  @date 2023-09-20
 */

public interface NbcioRedisListener {
	void onMessage(BaseMap message);
}
