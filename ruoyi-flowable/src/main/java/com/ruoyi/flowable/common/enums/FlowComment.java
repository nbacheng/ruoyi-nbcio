package com.ruoyi.flowable.common.enums;

/**
 * 流程意见类型
 *
 * @author nbacheng
 * @date 2023-09-25
 */
public enum FlowComment {

	/**
     * 说明
     */
    NORMAL("1", "正常意见"),
    REBACK("2", "退回意见"),
    REJECT("3", "驳回意见"),
    DELEGATE("4", "委派意见"),
    TRANSFER("5", "转办意见"),
    STOP("6", "终止流程"),
    REVOKE("7","撤回意见"),
	SKIP("8","跳过流程"),
	QJQ("9","前加签"),
    HJQ("10","后加签"),
	DSLJQ("11","多实例加签"),
	JUMP("12","跳转意见");

    /**
     * 类型
     */
    private final String type;

    /**
     * 说明
     */
    private final String remark;

    FlowComment(String type, String remark) {
        this.type = type;
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }
}
