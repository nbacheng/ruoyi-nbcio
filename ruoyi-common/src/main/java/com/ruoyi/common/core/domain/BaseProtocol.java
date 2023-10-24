package com.ruoyi.common.core.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;


@Data
public class BaseProtocol {
  private CmdKey cmdKey;

  public static <T> T parse(String msg, Class<T> clazz) {
    return (T) JSON.parseObject(msg, clazz);
  }
}
