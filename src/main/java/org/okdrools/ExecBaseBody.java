package org.okdrools;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 计算用中间对象
 *
 * @author Yangzy
 */
public class ExecBaseBody implements Serializable{

    /**
     * id
     */
    private static final long serialVersionUID = 4405339009615671759L;

    private HashMap<String, String> bodyMap;

    public ExecBaseBody() {
        bodyMap = new HashMap<>();
    }

    public void put(String key, String value) {
        bodyMap.put(key, value);
    }

    public String get(String key) {
        return bodyMap.get(key);
    }

    /**
     * 创建实例化语句
     *
     * @return
     */
    public String buildInstenceStr() {
        StringBuilder builder = new StringBuilder();
        builder.append("    ExecBaseBody execBody = new ExecBaseBody();\n");
        bodyMap.forEach((key, val)-> builder.append(String.format("    execBody.put(\"%s\", \"%s\");\n", key, val)));
        return builder.toString();
    }

}
