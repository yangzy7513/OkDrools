package org.okdrools;

import org.okdrools.annotation.Recorder;

/**
 * ExecRecorder工厂
 * 扫描@Recorder注解
 *
 * @author Yangzy
 */
public class ExecRecordManager {

    /**
     * 获取存储记录消息的recorder
     *
     * @return
     */
    public static ExecRecorder getRecorder() {
        Class c = ReflectUtils.getOneClassWithAnnotation(Recorder.class);
        try {
            return (ExecRecorder) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("未定义ExecRecorder实现类，查看是否添加注解@Recorder.");
        }
    }

}
