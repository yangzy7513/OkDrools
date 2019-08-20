package org.okdrools.impl;

import org.okdrools.ExecBaseBody;
import org.okdrools.ExecRecorder;
import org.okdrools.annotation.Recorder;

@Recorder
public class ExampleRecorder implements ExecRecorder {

    @Override
    public void save(ExecBaseBody execBaseBody, String book, String parameterId, Double value) {
        System.out.println("计算结果：" + value);
    }

    @Override
    public void recordErrorMsg(ExecBaseBody body, String book, Exception e) {

    }
}
