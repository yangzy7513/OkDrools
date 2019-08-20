package org.okdrools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.okdrools.fel.EvalEngine;
import org.okdrools.fel.FelEvalEngine;

import javax.script.ScriptException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Drools计算用定数及工具集
 * warning: drl文件的重要依赖类
 *
 * @author Yangzy
 */
public class DroolsUtils {

    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(DroolsUtils.class);

    /**
     * 运算全局变量名
     */
    public static final String CONTEXT_BODY = "CONTEXT_BODY";

    /**
     * 匹配公式中的参数
     */
    private static final Pattern PATTERN_PARAMETER = Pattern.compile("\\[[0-9a-zA-Z]+\\]");

    /**
     * 获取记录器
     */
    private static final ExecRecorder RECORDER = ExecRecordManager.getRecorder();

    /**
     * 计算引擎
     *
     * 当前提供两种版本
     * js版本：提供基础的四则运算和三角函数运算(JsEvalEngine)
     * fel版本：除了js版本有的功能，支持自定义函数
     */
    private static final ThreadLocal<EvalEngine> EVAL_ENGINE = ThreadLocal.withInitial(() -> new FelEvalEngine());

    /**
     * 执行计算
     *
     * @param str
     * @return
     */
    public static Double exec(Object str, String book, ExecBaseBody body) {
        Double dResult = 0D;
        EvalEngine jse = EVAL_ENGINE.get();
        try {
            String execStr = String.valueOf(str);
            jse.setEvalAttr(CONTEXT_BODY, body);
            Object result = jse.eval(execStr);
            dResult = Double.parseDouble(String.valueOf(result));
            if (Double.isNaN(dResult)) {
                return 0D;
            }
            if (dResult == Double.POSITIVE_INFINITY || dResult == Double.NEGATIVE_INFINITY) {
                throw new ScriptException("计算出现除0操作.");
            }
            if (jse instanceof FelEvalEngine) {
                if (execStr.contains("ROUND")) {
                    return dResult;
                }
            }
            DecimalFormat df = new DecimalFormat("0.00");
            return Double.valueOf(df.format(dResult));
        } catch (Exception e) {
            recordErrorMsg(body, book, e);
            LOG.error("【计算错误】" + str, e);
        }
        return dResult;
    }

    /**
     * 将公式中的ID转换为相应数值
     * @param formula
     * @param map
     * @return
     */
    public static String query(String formula, Map map) {
        Matcher matcher = PATTERN_PARAMETER.matcher(formula);
        while (matcher.find()) {
            String matcherStr = matcher.group();
            String key = StringUtils.strip(matcherStr, "[]");
            String val = String.valueOf(map.get(key));
            formula = formula.replace(matcherStr, val == null ? "0" : val);
        }
        return formula;
    }

    /**
     * 任意对象转double(通常其来自填报数据)
     *
     * @param obj
     * @return
     */
    public static double objToDouble(Object obj) {
        if (obj == null) {
            return 0;
        } else {
            return Double.valueOf(String.valueOf(obj));
        }
    }

    /**
     * 将计算结果存储到DB(附加打印日志)
     *
     * @param execBaseBody
     * @param book
     * @param parameterId
     * @param value
     */
    public static void save(ExecBaseBody execBaseBody, String book, String parameterId, Double value) {
        RECORDER.save(execBaseBody, book, parameterId, value);
    }

    /**
     * 记录错误消息
     *
     * @param body
     * @param book
     * @param e
     */
    private static void recordErrorMsg(ExecBaseBody body, String book, Exception e) {
        RECORDER.recordErrorMsg(body, book, e);
    }

}