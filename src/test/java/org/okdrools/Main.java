package org.okdrools;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;
import org.okdrools.impl.ExampleCondition;
import org.okdrools.impl.ExampleConfig;
import org.okdrools.impl.ExampleContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class Main {

    private static final String TEST_FORMULA = "[总成绩]=[语文]+[数学]+[英语]";


    @Test
    public void testCreateDrlFile() {
        OkDroolsConfig info = new ExampleConfig();
        String[] names = new String[] {"张三", "李四", "赵本山"};
        OkDroolsCondition condition = new ExampleCondition(names);

        // 1.公式转为对应参数ID
        String formula = strToUUID(TEST_FORMULA, true);

        // 2.创建规则文件
        ExecBaseBody body = new ExecBaseBody();
        body.put("period", "201908");
        body.put("ruleId", "12345678");

        BiMap paramIdMap = HashBiMap.create();
        paramIdMap.put("001", "语文");
        paramIdMap.put("002", "数学");
        paramIdMap.put("003", "英语");
        paramIdMap.put("004", "总成绩");
        FormulaToDrlConverter converter = new FormulaToDrlConverter(paramIdMap);
        converter.createDrl(info, condition, body, 0, formula);
    }

    @Test
    public void testExec() {
        AbstractDroolsContext adc = new ExampleContext();
        StatefulKnowledgeSession kSession = null;
        try {
            OkDroolsConfig okDroolsConfig = new ExampleConfig();
            kSession = adc.buildDroolsEnvironment();
            kSession.setGlobal("book", "001");
            adc.execDrools(kSession, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (kSession != null) {
                kSession.dispose();
            }
        }
    }

    @Test
    public void findAllDrlFile() {
        File rootDir = new File("E:\\drools");
        IOFileFilter drlFilter = FileFilterUtils.suffixFileFilter("drl");
        Collection<File> files = FileUtils.listFiles(rootDir, TrueFileFilter.INSTANCE, drlFilter);
        files.forEach(file -> System.out.println(file.getAbsolutePath()));
    }

    private static String strToUUID(String str, boolean isToUUid) {
        BiMap paramIdMap = TestData.getParameters();
        String cloneStr = str;
        String findPara = "";
        BiMap<String, String> inverseMap = isToUUid ? paramIdMap.inverse() : paramIdMap;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') {
                findPara = "";
            } else if (c == ']') {
                String uuid = inverseMap.get(findPara);
                if (uuid == null) continue;
                cloneStr = cloneStr.replace("[" + findPara + "]", "[" + uuid + "]");
            } else {
                findPara += c;
            }
        }
        return cloneStr;
    }

}
