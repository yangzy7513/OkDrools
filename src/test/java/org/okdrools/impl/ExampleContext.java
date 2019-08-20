package org.okdrools.impl;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;
import org.okdrools.AbstractDroolsContext;

import java.util.HashMap;

public class ExampleContext extends AbstractDroolsContext {


    @Override
    public void execDrools(StatefulKnowledgeSession kieSession, AgendaFilter filter, String... params) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("yearmonth", 201908 + "");
        map.put("name", "赵本山");
        map.put("001", 98);
        map.put("002", 86.569);
        map.put("003", 78);
        kieSession.insert(map);
        kieSession.fireAllRules();
        kieSession.dispose();
    }

    @Override
    public String[] excludeDrools() {
        return new String[0];
    }
}
