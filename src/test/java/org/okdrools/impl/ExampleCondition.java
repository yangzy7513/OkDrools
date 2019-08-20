package org.okdrools.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.okdrools.OkDroolsCondition;

public class ExampleCondition implements OkDroolsCondition {

    private String testYearMonth = "201908";

    private String functionName = "matchName" + testYearMonth;

    private String[] matchNames;

    public ExampleCondition(String[] matchNames) {
        this.matchNames = matchNames;
    }

    @Override
    public String name() {
        return testYearMonth + DigestUtils.shaHex(testYearMonth).toUpperCase().substring(0, 6);
    }

    @Override
    public String condition() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("    $p : Map(this[\"yearmonth\"] == %s);\n", 201908));
        builder.append("    eval(").append(functionName).append("($p.get(\"name\")));\n");
        return builder.toString();
    }

    @Override
    public String functions() {
        StringBuilder builder = new StringBuilder();
        builder.append("function boolean ").append(functionName).append("(Object name) {\n");
        for (int i = 0; i < matchNames.length; i++) {
            if (i == 0) {
                builder.append("    return \"").append(matchNames[i]).append("\".equals(name)");
            } else {
                builder.append("\n        || \"").append(matchNames[i]).append("\".equals(name)");
            }
        }
        builder.append(";\n}");
        return builder.toString();
    }
}
