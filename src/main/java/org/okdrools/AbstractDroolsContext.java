package org.okdrools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Drools环境，获取drl文件和执行具体运算
 *
 * @author Yangzy
 */
public abstract class AbstractDroolsContext extends OkDroolsConfig {

    private KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder();

    /**
     * 构建运行环境
     *
     * @return
     * @throws IOException
     */
    public StatefulKnowledgeSession buildDroolsEnvironment() throws IOException {
        return buildDroolsEnvironment(this);
    }

    public StatefulKnowledgeSession buildDroolsEnvironment(OkDroolsConfig config) throws IOException {
        KnowledgeBase knowledgeBase = getKnowledgeBase(config);
        return knowledgeBase.newStatefulKnowledgeSession();
    }

    protected KnowledgeBase getKnowledgeBase(OkDroolsConfig okDroolsConfig) throws IOException {
        Collection<File> ruleFiles = getRuleFiles(okDroolsConfig.drlFilePath() + File.separator);
        String[] excludeRuleIds = excludeDrools();
        String filePath;
        for (File file : ruleFiles) {
            filePath = file.getAbsolutePath().replace('\\', '/');
            if (excludeRuleIds != null && StringUtils.indexOfAny(filePath, excludeRuleIds) != -1) {
                // 排除项不做运算
                continue;
            }
            Resource ruleFile = ResourceFactory.newFileResource(filePath);
            kb.add(ruleFile, ResourceType.DRL);
        }
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        knowledgeBase.addKnowledgePackages(kb.getKnowledgePackages());
        return knowledgeBase;
    }

    /**
     * 执行计算逻辑
     *
     * @param session
     * @param filter 规则过滤
     * @param params 其它参数
     */
    public abstract void execDrools(StatefulKnowledgeSession session,
                                    AgendaFilter filter,
                                    String... params);

    /**
     * 排除计算的规则名称
     *
     * @return 通常是规则ID，因为文件名包含规则ID
     */
    public abstract String[] excludeDrools();

    /**
     * 获取目录下的规则文件
     *
     * @param drlPath 目录
     * @return
     * @throws IOException
     */
    private Collection<File> getRuleFiles(String drlPath) throws IOException {
        if (StringUtils.isEmpty(drlPath)) {
            throw new IllegalArgumentException("传入的文件夹目录为空!");
        }
        File rootDir = new File(drlPath);
        IOFileFilter drlFilter = FileFilterUtils.suffixFileFilter("drl");
        return FileUtils.listFiles(rootDir, TrueFileFilter.INSTANCE, drlFilter);
    }
}