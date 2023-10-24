package com.ruoyi.workflow.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.flowable.engine.runtime.ActivityInstance;


public interface FlowTaskMapper {
	List<ActivityInstance> queryActivityInstance(@Param("disActivityId") String disActivityId,
            @Param("processInstanceId")  String processInstanceId,
            @Param("endTime") Date endTime );
	 /**
     * 删除运行节点表信息
     * @param runActivityIds
     */
    void deleteRunActinstsByIds(List<String> runActivityIds);

    /**
     * 删除历史节点表信息
     * @param runActivityIds
     */
    void deleteHisActinstsByIds(List<String> runActivityIds);
    /**
     * 通过流程实例id，删除运行中的任务和历史相关数据，目前主要针对自定义业务
     * @param processInstanceId
     */
    void deleteAllHisAndRun(String processInstanceId);
    
    int querySubTaskByParentTaskId(@Param("parentTaskId")  String parentTaskId);
}
