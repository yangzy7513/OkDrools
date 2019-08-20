package org.okdrools;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TestData {

    public static final BiMap<String, String> parameters = HashBiMap.create();

    public static BiMap<String, String> getParameters() {
        parameters.put("001", "语文");
        parameters.put("002", "数学");
        parameters.put("003", "英语");
        parameters.put("004", "总成绩");
        return parameters;
    }

}
