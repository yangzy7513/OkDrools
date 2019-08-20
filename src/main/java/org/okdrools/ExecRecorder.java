package org.okdrools;

/**
 * 记录器
 *
 * @author Yangzy
 */
public interface ExecRecorder {

    /**
     * 记录公式中需要保存的结果到DB
     * @param execBaseBody 中间体
     * @param book 账套
     * @param parameterId 参数
     * @param value 值
     */
    void save(ExecBaseBody execBaseBody, String book, String parameterId, Double value);

    /**
     * 记录异常信息到DB中
     * @param body 中间体
     * @param book 账套
     * @param e 异常
     */
    void recordErrorMsg(ExecBaseBody body, String book, Exception e);

}
