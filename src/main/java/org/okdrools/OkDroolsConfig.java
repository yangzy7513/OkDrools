package org.okdrools;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * drl文件目录
 */
public class OkDroolsConfig {

    /**
     * 默认存储drl文件目录名称
     */
    private String defaultDirectoryName;

    public OkDroolsConfig() {
        this.defaultDirectoryName = "ok_drools";
    }

    public OkDroolsConfig(String defaultDirectoryName) {
        this.defaultDirectoryName = defaultDirectoryName;
    }

    /**
     * drl文件存储路径,默认返回系统临时文件盘路径
     *
     * @return
     */
    public String drlFilePath() {
        return FileUtils.getTempDirectory() + File.separator + defaultDirectoryName;
    }

}
