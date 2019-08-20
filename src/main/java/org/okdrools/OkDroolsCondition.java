package org.okdrools;

import java.util.UUID;

public interface OkDroolsCondition {

    /**
     * 规则名称
     *
     * @return
     */
    default String name() {
        return UUID.randomUUID().toString();
    }

    /**
     * 规则条件部分
     *
     * @return
     */
    String condition();

    /**
     * 使用方法
     *
     * @return
     */
    String functions();

}
