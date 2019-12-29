package org.okdrools;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公式转DRL文件转换器
 * ！！！！注意：不要调整方法内部代码中的空白格
 *
 * @author Yangzy
 */
public class FormulaToDrlConverter {

    /**
     * 日志
     */
    private static Log LOG = LogFactory.getLog(FormulaToDrlConverter.class);

    /**
     * 每次生成新的变量名使用
     */
    private AtomicInteger ait = new AtomicInteger();

    /**
     * 存储公式中的原变量与新变量名称映射
     */
    private Map<String, String> variableMap = new HashMap<>();

    /**
     * 参数与ID映射
     */
    private BiMap<String, String> paramIdMap;

    /**
     * 已经定义过的变量（避免重复定义报错）
     */
    private List<String> definedVariables = new ArrayList<>();

    /**
     * builder
     */
    private KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    /**
     * 中间变量前缀
     */
    private static final String PREFIX_VAR = "_var";

    /**
     * 中间变量形式
     */
    private static final String REGEX_VAR = "\\_var[0-9]+";

    /**
     * 匹配变量
     */
    private static final Pattern PATTERN_VAR = Pattern.compile(REGEX_VAR);

    /**
     * 实例化参数
     *
     * @param paramIdMap 参数表
     */
    public FormulaToDrlConverter(BiMap<String, String> paramIdMap) {
        this.paramIdMap = paramIdMap;
    }

    /**
     * 编译生成drl
     *
     * @param info
     * @param condition
     * @param salience
     * @param formula
     * @return
     */
    public synchronized Boolean createDrl(
                             OkDroolsConfig info,
                             OkDroolsCondition condition,
                             ExecBaseBody execBaseBody,
                             int salience,
                             String formula) {
        StringBuilder drl = new StringBuilder();
        drl.append(commonCopyRight());
        drl.append("package org.okdrools\n\n");
        drl.append("import java.util.*;\n");
        drl.append("import org.okdrools.ExecBaseBody;\n");
        drl.append("import static org.okdrools.DroolsUtils.*;\n\n");
        drl.append("global java.lang.String book;\n\n");
        if (StringUtils.isNotEmpty(condition.functions())) {
            // 自定义方法
            drl.append(condition.functions());
            drl.append("\n\n");
        }
        // 规则部分
        drl.append("rule \"" + UUID.randomUUID().toString() + "\"\n");
        drl.append("salience "+ (salience != 0 ? "-"+ salience : salience) +"\n");
        drl.append("when\n");
        drl.append(condition.condition());
        drl.append("then\n");
        drl.append(execBaseBody.buildInstenceStr());
        // 切开单个公式
        String[] formulas = formula.split(";");
        Map<String, String> map;
        String funcPara;
        for (String s : formulas) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            // 公式左侧变量是否需要存储到DB
            boolean isNeedSave = false;
            s = s.replaceAll("\\$", "_").trim();
            if (s.contains("IF")) {
                map = handleIf(s);
                drl.append(map.get("drl"));
                drl.append("\n");
            } else {
                if (variableMap != null && variableMap.size() > 0) {
                    s = replaceNewVar(s);
                }
                String maybePara = "";
                if (s.contains("=")) {
                    // 如果公式形式为"[参数]=",则此值用于存储到DB
                    maybePara = StringUtils.strip(s.split("=")[0].trim(), "[]").trim();
                    if (s.startsWith("[") && paramIdMap.containsKey(maybePara)) {
                        funcPara = "_" + maybePara;
                        isNeedSave = true;
                    } else {
                        funcPara = PREFIX_VAR + ait.getAndIncrement();
                        if (!variableMap.containsKey(maybePara)) {
                            // 此时的变量作为可能进入下一轮计算用，而计算出来的数值为Double
                            variableMap.put(maybePara, funcPara);
                        }
                    }
                    s = s.substring(s.indexOf("=") + 1).replaceAll("\\n", "");
                } else {
                    funcPara = PREFIX_VAR + ait.getAndIncrement();
                }
                s = strToFormula(s);
                if (!definedVariables.contains(funcPara)) {
                    definedVariables.add(funcPara);
                    drl.append("    double " + funcPara + " = " + s + ";\n");
                } else {
                    drl.append("    " + funcPara + " = " + s + ";\n");
                }
                if (isNeedSave) {
                    drl.append(String.format("    $p.put(\"%s\", %s + \"\");\n", maybePara, funcPara));
                    drl.append(String.format("    save(execBody, book, \"%s\", %s);\n",
                             maybePara, funcPara));
                }
            }
        }
        drl.append("    retract($p);\n");
        drl.append("end");
        
        try {
            // 编译校验
            Resource ruleResource = ResourceFactory.newByteArrayResource(drl.toString().getBytes());
            kBuilder.add(ruleResource, ResourceType.DRL);
            if (kBuilder.hasErrors()) {
                LOG.error("编译drl文件失败！");
                LOG.error(kBuilder.getErrors().toString());
                kBuilder.undo();
                return false;
            }
            // 生成drl文件
            String fileName = condition.name() + ".drl";
            String tempFilePath = info.drlFilePath() + "\\";
            writeDrlToFile(drl.toString(), tempFilePath, fileName);
            kBuilder.undo();
            return true;
        } finally {
            variableMap.clear();
            definedVariables.clear();
        }
    }

    /**
     * IF条件句
     *
     * @param str
     * @return list的0项为drl部分，此后为if的中间变量，用于计算
     */
    private Map<String, String> handleIf(String str) {
        Map<String, String> parameters = new HashMap<>();
        str = replaceNewVar(str);
        str = replaceNewVar(str).replaceAll("\n", "");
        str = str.replaceAll("ELSE IF", "    } else if(")
                .replaceAll("ELSE THEN", "    } else {")
                .replaceAll("IF", "    if(")
                .replaceAll("THEN", ") {");
        str += "}\n";
        str = str.replaceAll("\\[", "objToDouble(\\$p.get(\"")
                .replaceAll("\\]", "\"))");
        // 变量名
        String varName;
        // 截取LHS部分
        StringBuilder rhs = new StringBuilder();
        // 新增变量定义部分
        StringBuilder varHeader = new StringBuilder();
        StringBuilder build = new StringBuilder();
        boolean write = true;
        for (int idx = 0; idx < str.length(); idx++) {
            char c = str.charAt(idx);
            if (c == '{') {
                rhs = new StringBuilder();
                build.append("{");
                write = false;
            } else if (c == '}') {
                String[] childs = rhs.toString().split(",");
                StringBuilder sdk = new StringBuilder();
                for (String chils : childs) {
                    String leftPara = chils.split("=")[0].trim();
                    if (!parameters.containsKey(leftPara)) {
                        // 说明左侧是临时变量
                        if (!variableMap.containsKey(leftPara)) {
                            varName = PREFIX_VAR + ait.getAndIncrement();
                            varHeader.append("    double " + varName + " = 0;\n");
                            variableMap.put(leftPara, varName);
                        }
                    }
                    sdk.append("     ")
                            .append(variableMap.get(leftPara))
                            .append(" = ")
                            .append(chils.split("=")[1].trim())
                            .append(";");
                }
                build.append(sdk).append(c);
                write = true;
            } else {
                rhs.append(c);
                if (write) {
                    build.append(c);
                }
            }
        }
        str = build.toString();
        // 美化
        str = str.replaceAll(";", ";\n    ").replaceAll("\\{", "{\n    ");
        variableMap.put("drl", varHeader.toString() + str);
        return variableMap;
    }

    /**
     * 替换公式中自定义变量名为新的变量名
     *
     * @param str
     * @return
     */
    private String replaceNewVar(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        for (Map.Entry<String, String> entry : variableMap.entrySet()) {
            str = str.replaceAll(entry.getKey(), entry.getValue());
        }
        return str;
    }

    /**
     * 转换可编译公式
     *
     * @param str
     * @return
     */
    private String strToFormula(String str) {
        if (!str.contains("[") && !str.matches("[\\S\\W]+[A-Z]+\\([\\S\\W]+")) {
            // 不包含参数项目也不包含自定义函数
            return str;
        }
        StringBuilder builder = new StringBuilder();
        // 切分出非变量部分
        Iterable<String> splitResult = Splitter.onPattern(REGEX_VAR)
                .trimResults()
                .omitEmptyStrings()
                .split(str);
        // 筛选变量部分(形式为：_var[0-9])
        Matcher matcher = PATTERN_VAR.matcher(str);
        Iterator<String> iterator = splitResult.iterator();
        List<String> groupList = new ArrayList<>();
        while (matcher.find()) {
            groupList.add(matcher.group());
        }
        Iterator<String> listIterator = groupList.iterator();
        // 拼接
        while (iterator.hasNext()) {
            String iStr = iterator.next();
            if (listIterator.hasNext()) {
                String mFind = listIterator.next();
                if (str.indexOf(mFind) < str.indexOf(iStr)) {
                    builder.append(mFind).append("+\"").append(iStr).append("\"+");
                } else {
                    builder.append("+\"").append(iStr).append("\"+").append(mFind);
                }
                listIterator.remove();
            } else {
                builder.append("+\"").append(iStr).append("\"+");
            }
        }
        // 筛选
        int start = 0;
        int endS = builder.length();
        String s = builder.toString();
        if (s.startsWith("+")) {
            start++;
        }
        if (s.endsWith("+")) {
            endS--;
        }
        String result = s.substring(start, endS);
        String execStr = String.format("exec(query(%s, $p), book, execBody)", result);
        return execStr;
    }

    /**
     * 著作权及提示
     *
     * @return
     */
    private String commonCopyRight () {
        return "/**********************************************************\n"
             + " * The following code is automatically generated by the program.\n"
             + " * Please do not try to change or delete this file.\n"
             + " **********************************************************/\n";
    }

    /**
     * 写入DRL文件
     *
     * @param str
     * @param filePath
     */
    private File writeDrlToFile(String str, String filePath, String fileName) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File drlFile = new File(filePath + fileName);
        try {
            FileUtils.write(drlFile, str, "utf-8");
            LOG.info("生成drools文件:" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drlFile;
    }

}
