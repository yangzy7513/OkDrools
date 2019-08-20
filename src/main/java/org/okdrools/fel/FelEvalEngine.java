package org.okdrools.fel;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.function.Function;
import com.greenpineyu.fel.parser.FelNode;
import org.apache.commons.collections4.CollectionUtils;
import org.okdrools.ReflectUtils;
import org.okdrools.annotation.FelFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * FEL计算引擎，支持自定义函数和高级功能
 *
 * @author Yangzy
 */
public class FelEvalEngine implements EvalEngine {

    private static final Logger LOG = LoggerFactory.getLogger(FelEvalEngine.class);

    private FelEngine felEngine;

    public FelEvalEngine() {
        felEngine = new FelEngineImpl();
        Round round = new Round();
        felEngine.addFun(round);
        loadOtherFunction();
    }

    /**
     * 加载其他包下的方法类
     * 默认扫描路径为hospital.beijing.*.apps.func
     */
    public void loadOtherFunction() {
        List<Class> classes = ReflectUtils.getClassesWithAnnotation(FelFunction.class);
        if (CollectionUtils.isNotEmpty(classes)) {
            for (Class c : classes) {
                try {
                    felEngine.addFun((Function) c.newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保留小数位
     */
    static class Round extends SimpleFunction {

        @Override
        public String getName() {
            return "ROUND";
        }

        @Override
        public Object call(FelNode node, FelContext context) {
            List<FelNode> args = ensureValid(node, 2);
            FelNode arg1 = args.get(0);
            FelNode arg2 = args.get(1);
            BigDecimal value = new BigDecimal(String.valueOf(arg1.eval(context)));
            int ronudNum = Integer.valueOf(String.valueOf(arg2.eval(context)));
            return value.setScale(ronudNum, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    @Override
    public Object eval(String script) {
        script = script.replace("null", "0");
        return felEngine.eval(script);
    }

    @Override
    public void setEvalAttr(String name, Object value) {
        FelContext context = felEngine.getContext();
        context.set(name, value);
    }

    @Override
    public Object getEvalAttr(String name) {
        FelContext context = felEngine.getContext();
        return context.get(name);
    }

}
