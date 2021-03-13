package cgeo.geocaching.utils.expressions;

import cgeo.geocaching.utils.functions.Func2;
import cgeo.geocaching.utils.functions.Func3;

import java.util.ArrayList;
import java.util.List;

public class LambdaExpression<P, R> implements IExpression<LambdaExpression<P, R>> {


    private final String typeId;
    private final Func2<String, P, R> valueFunction;
    private final Func3<String, P, List<R>, R> groupFunction;

    private final List<LambdaExpression<P, R>> children = new ArrayList<>();

    private String config;

    public LambdaExpression(final String typeId, final Func2<String, P, R> function) {
        this.typeId = typeId;
        this.valueFunction = function;
        this.groupFunction = null;
    }

    public LambdaExpression(final String typeId, final Func3<String, P, List<R>, R> function) {
        this.typeId = typeId;
        this.valueFunction = null;
        this.groupFunction = function;
    }

    @Override
    public void setConfig(final String config) {
        this.config = config;
    }

    @Override
    public String getConfig() {
        return config;
    }

    @Override
    public void addChild(final LambdaExpression<P, R> child) {
        children.add(child);
    }

    @Override
    public List<LambdaExpression<P, R>> getChildren() {
        return children;
    }

    @Override
    public String getTypeId() {
        return typeId;
    }

    public R call(final P param) {
        if (valueFunction != null) {
            return valueFunction.call(config, param);
        }
        final List<R> result = new ArrayList<>();
        for (LambdaExpression<P, R> child : getChildren()) {
            result.add(child.call(param));
        }
        return groupFunction.call(getConfig(),  param, result);
    }
}
