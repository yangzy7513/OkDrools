package org.okdrools.fel;

/**
 * 计算引擎SPI
 *
 * @author Yangzy
 */
public interface EvalEngine {

    /**
     * 执行表达式计算
     *
     * @param script 脚本
     * @return 计算结果
     * @throws Exception
     */
    Object eval(String script) throws Exception;

    /**
     * 设置运算变量
     *
     * @param name 变量名
     * @param value 变量值
     */
    void setEvalAttr(String name, Object value);

    /**
     * 获取运算变量
     *
     * @param name 变量名
     * @return 变量值
     */
    Object getEvalAttr(String name);

}
