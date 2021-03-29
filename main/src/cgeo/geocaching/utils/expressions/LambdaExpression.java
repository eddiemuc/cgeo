package cgeo.geocaching.utils.expressions;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cgeo.geocaching.utils.functions.Func2;
import cgeo.geocaching.utils.functions.Func3;

public class LambdaExpression<P, R> implements IExpression<LambdaExpression<P, R>> {


    private final String id;
    private final ExpressionType expressionType;
    private final Func2<String, P, R> simpleFunction;
    private final Func3<String, P, R, R> unnaryFunction;
    private final Func3<String, P, ImmutablePair<R, R>, R> binaryFunction;
    private final int binaryOperatorPriority;
    private final boolean binaryOperatorOrderSensitive;

    private LambdaExpression<P, R> childLeft;
    private LambdaExpression<P, R> childRight;

    private String config;

    public static <P, R> LambdaExpression<P, R> createSimple(final String id, final Func2<String, P, R> function) {
        return new LambdaExpression<>(id, ExpressionType.SIMPLE, function, null, null, -1, false);
    }

    public static <P, R> LambdaExpression<P, R> createBinary(final String id, final Func3<String, P, ImmutablePair<R, R>, R> function) {
        return createBinary(id, 10, true, function);
    }

    public static <P, R> LambdaExpression<P, R> createBinary(final String id, final int binaryOperatorPriority, final boolean binaryOperatorOrderSensitive, final Func3<String, P, ImmutablePair<R, R>, R> function) {
        return new LambdaExpression<>(id, ExpressionType.OPERATOR_BINARY, null, null, function, binaryOperatorPriority, binaryOperatorOrderSensitive);
    }

    public static <P, R> LambdaExpression<P, R> createUnary(final String id, final Func3<String, P, R, R> function) {
        return new LambdaExpression<>(id, ExpressionType.OPERATOR_UNARY, null, function, null, -1, false);
    }

    public LambdaExpression(final String id, final ExpressionType type, final Func2<String, P, R> funcSimple, final Func3<String, P, R, R> funcUnary, final Func3<String, P, ImmutablePair<R, R>, R> funcBinary,
                            final int binaryOperatorPriority, final boolean binaryOperatorOrderSensitive) {
        this.id = id;
        this.simpleFunction = funcSimple;
        this.binaryFunction = funcBinary;
        this.unnaryFunction = funcUnary;
        this.expressionType = type;
        this.binaryOperatorPriority = binaryOperatorPriority;
        this.binaryOperatorOrderSensitive = binaryOperatorOrderSensitive;
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
    public void addChildren(final LambdaExpression<P, R> left, final LambdaExpression<P, R> right) {
        this.childLeft = left;
        this.childRight = right;
    }

    @Override
    public LambdaExpression<P, R> getChildLeft() {
        return childLeft;
    }

    @Override
    public LambdaExpression<P, R> getChildRight() {
        return childRight;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ExpressionType getType() {
        return expressionType;
    }

    @Override
    public int getOperatorBinaryPriority() {
        return binaryOperatorPriority;
    }

    @Override
    public boolean getOperatorBinaryOrderSensitive() {
        return binaryOperatorOrderSensitive;
    }

    public R call(final P param) {
        if (simpleFunction != null) {
            return simpleFunction.call(config, param);
        }
        if (unnaryFunction != null) {
            return unnaryFunction.call(config, param, childLeft.call(param));
        }
        return binaryFunction.call(getConfig(),  param, new ImmutablePair<>(childLeft.call(param), childRight.call(param)));
    }


}
