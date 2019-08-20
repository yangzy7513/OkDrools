package org.okdrools;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具类
 *
 * @author yangzy
 */
public class ReflectUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 返回带有指定注解的类集合
     *
     * @param annotation 注解类
     * @return
     */
    public static List<Class> getClassesWithAnnotation(Class annotation) {
        List<Class> classes = new ArrayList<>();
        ClassPath classPath;
        try {
            classPath = ClassPath.from(ReflectUtils.class.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> allClasses = classPath.
                    getTopLevelClassesRecursive("org.okdrools");
            for (ClassPath.ClassInfo classInfo : allClasses) {
                try {
                    Class c = classInfo.load();
                    if (c.getAnnotation(annotation) != null) {
                        classes.add(c);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            return classes;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 返回带有指定注解的类
     *
     * @param annotation 注解类
     * @return
     */
    public static Class getOneClassWithAnnotation(Class annotation) {
        List<Class> classes = getClassesWithAnnotation(annotation);
        if (CollectionUtils.isNotEmpty(classes)) {
            return classes.get(0);
        }
        return null;
    }
}
