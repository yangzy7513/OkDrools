package org.okdrools.fel;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static javax.script.ScriptContext.GLOBAL_SCOPE;

/**
 * javascript计算引擎，支持基本运算表达式，不支持自定义函数
 *
 * @author Yangzy
 */
public class JsEvalEngine implements EvalEngine {

    private ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

    @Override
    public Object eval(String script) throws Exception{
        return jse.eval(script);
    }

    @Override
    public void setEvalAttr(String name, Object value) {
        ScriptContext sc = jse.getContext();
        sc.getScopes().get(0);
        sc.setAttribute(name, value, GLOBAL_SCOPE);
    }

    @Override
    public Object getEvalAttr(String name) {
        ScriptContext sc = jse.getContext();
        return sc.getAttribute(name, GLOBAL_SCOPE);
    }
}
