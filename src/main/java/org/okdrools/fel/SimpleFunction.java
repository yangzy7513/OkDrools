package org.okdrools.fel;

import com.greenpineyu.fel.compile.SourceBuilder;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.exception.ParseException;
import com.greenpineyu.fel.function.Function;
import com.greenpineyu.fel.parser.FelNode;

import java.util.List;

/**
 * 定义基本计算用方法
 *
 * @author Yangzy
 */
public abstract class SimpleFunction implements Function {

    /**
     * 判断是否满足参数个数
     * @param node
     * @param num
     * @return
     */
    protected List<FelNode> ensureValid(FelNode node, int num) {
        List<FelNode> args = node.getChildren();
        if(args==null||args.size() != num){
            throw new ParseException(getName() + "参数个数应为:" + num);
        }
        return args;
    }

    @Override
    public SourceBuilder toMethod(FelNode node, FelContext ctx) {
        return null;
    }

}
